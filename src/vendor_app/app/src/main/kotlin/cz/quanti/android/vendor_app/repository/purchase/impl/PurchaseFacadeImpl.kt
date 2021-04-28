package cz.quanti.android.vendor_app.repository.purchase.impl

import android.annotation.SuppressLint
import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseRepository
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.utils.BlockedCardError
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.CompletableSource
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
        //todo remove later
        return purchaseRepo.getAllPurchases().flatMapCompletable { purchases ->
            purchaseRepo.getRedemptionCandidatePurchases(purchases).flatMapCompletable {
                Log.d("xxx",it.first.toString())
                if (it.second.isEmpty()) {
                    Log.d("xxx", "empty")
                    throw VendorAppException("no purchases").apply {
                        apiError = true
                    }
                }else {
                    Log.d("xxx",it.second.toString())
                    Completable.complete()
                }
            }
            Completable.complete()
        }.andThen(

        purchaseRepo.getAllPurchases().flatMapCompletable { purchases ->
            purchaseRepo.sendVoucherPurchasesToServer(purchases.filter { it.vouchers.isNotEmpty() })
                .flatMapCompletable { responseCode ->
                    if (isPositiveResponseHttpCode(responseCode)) {
                        purchaseRepo.deleteAllVoucherPurchases().andThen(
                        Observable.fromIterable(purchases.filter { it.smartcard != null })
                            .flatMapCompletable { purchase ->
                                purchaseRepo.sendCardPurchaseToServer(purchase)
                                    .flatMapCompletable { responseCode ->
                                        if (isPositiveResponseHttpCode(responseCode)) {
                                            purchaseRepo.deleteCardPurchase(purchase)
                                        } else {
                                            throw VendorAppException("Could not send card purchases to the server").apply {
                                                apiError = true
                                                apiResponseCode = responseCode
                                            }
                                        }
                                    }
                            }
                        )
                    } else {
                        throw VendorAppException("Could not send voucher purchases to the server").apply {
                            apiError = true
                            apiResponseCode = responseCode
                        }
                    }

                }
        }
        )
    }

    private fun deleteAllPurchases(): Completable {
        return purchaseRepo.deleteAllPurchases()
    }
}
