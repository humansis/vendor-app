package cz.quanti.android.vendor_app.repository.purchase.impl

import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseRepository
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.utils.BlockedCardError
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import quanti.com.kotlinlog.Log

class PurchaseFacadeImpl(
    private val purchaseRepo: PurchaseRepository,
    private val cardRepo: CardRepository
) : PurchaseFacade {

    override fun savePurchase(purchase: Purchase): Completable {
        if (purchase.smartcard != null) {
            return cardRepo.isBlockedCard(purchase.smartcard!!).flatMapCompletable { itsBlocked ->
                if(itsBlocked) {
                    throw BlockedCardError("This card is tagged as blocked on the server")
                } else {
                    purchaseRepo.savePurchase(purchase)
                }
            }
        } else {
            return purchaseRepo.savePurchase(purchase)
        }
    }

    override fun syncWithServer(): Completable {
        return sendPurchasesToServer()
            .andThen(deleteAllPurchases())
    }

    override fun isSyncNeeded(): Single<Boolean> {
        return purchaseRepo.getPurchasesCount().map { it > 0 }
    }

    private fun sendPurchasesToServer(): Completable {
        val invalidPurchases =  mutableListOf<Purchase>()
        return purchaseRepo.getAllPurchases().flatMapCompletable { allPurchases ->
            val purchases = allPurchases.toMutableList()
            purchases.filter { it.products.isEmpty()}.forEach {
                // delete voucher purchases that contain 0 products
                if (it.vouchers.isNotEmpty()) {
                    purchaseRepo.deleteVoucherPurchase(it)
                }
                // delete card purchases that contain 0 products
                if (it.smartcard != null) {
                    purchaseRepo.deleteCardPurchase(it)
                }
                Log.d("Invalid Purchase:","Purchase ${it.dbId} created at ${it.createdAt} has no products")
                purchases.remove(it)
            }
            purchaseRepo.sendVoucherPurchasesToServer(purchases.filter { it.vouchers.isNotEmpty() })
                .flatMapCompletable { responseCode ->
                    if (isPositiveResponseHttpCode(responseCode)) {
                        purchaseRepo.deleteAllVoucherPurchases().andThen(
                        Observable.fromIterable(purchases.filter { it.smartcard != null })
                            .flatMapCompletable { purchase ->
                                purchaseRepo.sendCardPurchaseToServer(purchase)
                                    .flatMapCompletable { responseCode ->
                                        if (isPositiveResponseHttpCode(responseCode)) {
                                            Log.d("xxx","Received code: $responseCode when trying to sync ${purchase.smartcard}")
                                            purchaseRepo.deleteCardPurchase(purchase)
                                        } else {
                                            Log.d("xxx","Received code: $responseCode when trying to sync ${purchase.smartcard}")
                                            invalidPurchases.add(purchase)
                                            Completable.complete()
                                        }
                                    }
                            }
                            //throw exception after all purchases has been iterated
                            .doOnComplete {
                                if (invalidPurchases.isNotEmpty()) {
                                    throw VendorAppException("Could not send card purchases to the server.").apply {
                                        apiError = true
                                        apiResponseCode = responseCode
                                    }
                                }
                            }
                        )
                    } else {
                        throw VendorAppException("Could not send voucher purchases to the server. Received error code: $responseCode").apply {
                            apiError = true
                            apiResponseCode = responseCode
                        }
                    }

                }
        }
    }

    private fun deleteAllPurchases(): Completable {
        return purchaseRepo.deleteAllPurchases()
    }
}
