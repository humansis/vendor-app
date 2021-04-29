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
        return if (purchase.smartcard != null) {
            cardRepo.isBlockedCard(purchase.smartcard!!).flatMapCompletable { itsBlocked ->
                if(itsBlocked) {
                    throw BlockedCardError("This card is tagged as blocked on the server")
                } else {
                    purchaseRepo.savePurchase(purchase)
                }
            }
        } else {
            purchaseRepo.savePurchase(purchase)
        }
    }

    override fun syncWithServer(vendorId: Int): Completable {
        return preparePurchases()
            .andThen(deleteAllPurchases())
            .andThen(getInvoices(vendorId))
            .andThen(getTransactions(vendorId))

    }

    override fun isSyncNeeded(): Single<Boolean> {
        return purchaseRepo.getPurchasesCount().map { it > 0 }
    }

    override fun unsyncedPurchases(): Single<List<Purchase>> {
        return purchaseRepo.getAllPurchases()
    }

    private fun preparePurchases(): Completable {
        return purchaseRepo.getAllPurchases().flatMapCompletable { purchases ->
            Observable.fromIterable(purchases.filter { it.products.isEmpty() })
                .flatMapCompletable {
                    Log.d(
                        TAG,
                        "Purchase ${it.dbId} created at ${it.createdAt} has no products"
                    )
                    purchaseRepo.deletePurchase(it)
                }
                .andThen(
                    sendPurchasesToServer()
                )
        }
    }

    private fun sendPurchasesToServer(): Completable {
        val invalidPurchases = mutableListOf<Purchase>()

        return purchaseRepo.getAllPurchases().flatMapCompletable { purchases ->
            if (purchases.isEmpty()) {
                Completable.complete()
            } else {
                val voucherPurchases = purchases.filter { it.vouchers.isNotEmpty() }
                purchaseRepo.sendVoucherPurchasesToServer(voucherPurchases)
                    .flatMapCompletable { responseCode ->
                        if (isPositiveResponseHttpCode(responseCode)) {
                            purchaseRepo.deleteAllVoucherPurchases()
                        } else {
                            invalidPurchases.addAll(voucherPurchases)
                            Completable.complete()
                        }
                    }.andThen(
                        Observable.fromIterable(purchases.filter { it.smartcard != null })
                            .flatMapCompletable { purchase ->
                                purchaseRepo.sendCardPurchaseToServer(purchase)
                                    .flatMapCompletable { responseCode ->
                                        Log.d(
                                            TAG,
                                            "Received code $responseCode when trying to sync purchase ${purchase.dbId} by ${purchase.smartcard}"
                                        )
                                        if (isPositiveResponseHttpCode(responseCode)) {
                                            purchaseRepo.deleteCardPurchase(purchase)
                                        } else {
                                            invalidPurchases.add(purchase)
                                            Completable.complete()
                                        }
                                    }
                            }
                            //throw exception after all purchases has been iterated
                            .doOnComplete {
                                if (invalidPurchases.isNotEmpty()) {
                                    throw VendorAppException("Could not send purchases to the server.").apply {
                                        apiError = true
                                    }
                                }
                            }
                    )
            }
        }
    }

    private fun deleteAllPurchases(): Completable {
        return purchaseRepo.deleteAllPurchases()
    }

    private fun getInvoices(vendorId: Int): Completable {
        return purchaseRepo.getInvoices(vendorId).flatMapCompletable {
            val responseCode = it.first
            val invoicesList = it.second
            if (isPositiveResponseHttpCode(responseCode)) {
                //todo naplnit databázi ...if invoiceslist.isEmpty() atd
                Completable.complete()
            } else {
                throw VendorAppException("Received code $responseCode when trying download invoices.").apply {
                    apiError = true
                    apiResponseCode = responseCode
                }
            }
        }
    }

    private fun getTransactions(vendorId: Int): Completable {
        return purchaseRepo.getTransactions(vendorId).flatMapCompletable { it ->
            val responseCode = it.first
            val transactionsList = it.second
            if (isPositiveResponseHttpCode(responseCode)) {
                if  (transactionsList.isNotEmpty()) {
                    Observable.fromIterable(transactionsList).flatMapCompletable { transactions ->
                        purchaseRepo.getPurchasesById(transactions.purchaseIds).flatMapCompletable { response ->
                            val purchasesList = response.second
                            if (isPositiveResponseHttpCode(response.first)) {
                                if (purchasesList.isEmpty()) {
                                    throw VendorAppException("No purchases").apply {
                                        apiError = true
                                    }
                                } else {
                                    //todo vyuzit purchases
                                    Completable.complete()
                                }
                            } else {
                                throw VendorAppException("Received code ${response.first} when trying download purchases.").apply {
                                    apiError = true
                                    apiResponseCode = responseCode
                                }
                            }
                        }
                    }
                } else {
                    //todo doresit
                    Completable.complete()
                }
            } else {
                throw VendorAppException("Received code $responseCode when trying download transactions.").apply {
                    apiError = true
                    apiResponseCode = responseCode
                }
            }
        }
    }

    companion object {
        private val TAG = PurchaseFacadeImpl::class.java.simpleName
    }

}
