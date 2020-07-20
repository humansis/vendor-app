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

    private fun sendPurchasesToServer(): Completable {
        return purchaseRepo.getAllPurchases().flatMapCompletable { purchases ->
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
    }

    private fun deleteAllPurchases(): Completable {
        return purchaseRepo.deleteAllPurchases()
    }
}
