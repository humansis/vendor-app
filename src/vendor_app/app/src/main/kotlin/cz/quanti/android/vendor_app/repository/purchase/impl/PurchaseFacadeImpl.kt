package cz.quanti.android.vendor_app.repository.purchase.impl

import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseRepository
import cz.quanti.android.vendor_app.repository.purchase.dto.Invoice
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.Transaction
import cz.quanti.android.vendor_app.repository.purchase.dto.TransactionPurchase
import cz.quanti.android.vendor_app.repository.purchase.dto.api.InvoiceApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.TransactionPurchaseApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.TransactionApiEntity
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
            .andThen(retrieveInvoices(vendorId))
            .andThen(retrieveTransactions(vendorId))

    }

    override fun isSyncNeeded(): Single<Boolean> {
        return purchaseRepo.getPurchasesCount().map { it > 0 }
    }

    override fun unsyncedPurchases(): Single<List<Purchase>> {
        return purchaseRepo.getAllPurchases()
    }

    override fun getInvoices(): Single<List<Invoice>> {
        return purchaseRepo.getInvoices()
    }

    override fun getTransactions(): Single<List<Transaction>> {
        return purchaseRepo.getTransactions()
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

    private fun retrieveInvoices(vendorId: Int): Completable {
        return purchaseRepo.retrieveInvoices(vendorId).flatMapCompletable {
            val responseCode = it.first
            val invoicesList = it.second
            if (isPositiveResponseHttpCode(responseCode)) {
                if (invoicesList.isNotEmpty()) {
                    actualizeInvoiceDatabase(invoicesList)
                } else {
                        //todo doresit aby exceptiony neprerusovaly sync
                    throw VendorAppException("No invoices").apply {
                        apiError = true
                    }
                }
            } else {
                //todo doresit aby exceptiony neprerusovaly sync
                throw VendorAppException("Received code $responseCode when trying download invoices.").apply {
                    apiError = true
                    apiResponseCode = responseCode
                }
            }
        }
    }

    private fun retrieveTransactions(vendorId: Int): Completable {
        return purchaseRepo.retrieveTransactions(vendorId).flatMapCompletable {
            val responseCode = it.first
            val transactionsList = it.second
            if (isPositiveResponseHttpCode(responseCode)) {
                deleteAllTransactions()
                if  (transactionsList.isNotEmpty()) {
                    var id: Long = 1
                    Observable.fromIterable(transactionsList).flatMapCompletable { transactions ->
                        purchaseRepo.retrieveTransactionsPurchasesById(transactions.purchaseIds).flatMapCompletable { response ->
                            val transactionPurchasesList = response.second
                            if (isPositiveResponseHttpCode(response.first)) {
                                if (transactionPurchasesList.isNotEmpty()) {
                                    Single.fromCallable { saveTransactionToDb(transactions, id) }.flatMapCompletable { transactionId ->
                                        id++
                                        actualizeTransactionPurchaseDatabase(transactionPurchasesList, transactionId)
                                    }
                                } else {
                                    //todo doresit aby exceptiony neprerusovaly sync
                                    throw VendorAppException("No purchases").apply {
                                        apiError = true
                                    }
                                }
                            } else {
                                //todo doresit aby exceptiony neprerusovaly sync
                                throw VendorAppException("Received code ${response.first} when trying download purchases.").apply {
                                    apiError = true
                                    apiResponseCode = responseCode
                                }
                            }
                        }
                    }
                } else {
                    //todo doresit aby exceptiony neprerusovaly sync
                    throw VendorAppException("No transactions").apply {
                        apiError = true
                    }
                }
            } else {
                //todo doresit aby exceptiony neprerusovaly sync
                throw VendorAppException("Received code $responseCode when trying download transactions.").apply {
                    apiError = true
                    apiResponseCode = responseCode
                }
            }
        }
    }

    private fun actualizeInvoiceDatabase(invoices: List<InvoiceApiEntity>?): Completable {
        return purchaseRepo.deleteInvoices().andThen(
            Observable.fromIterable(invoices).flatMapCompletable { invoice ->
                Completable.fromSingle( purchaseRepo.saveInvoice(invoice) )
            })
    }

    private fun deleteAllTransactions(): Completable {
        return purchaseRepo.deleteTransactions()
    }

    private fun saveTransactionToDb(transaction: TransactionApiEntity, transactionId: Long): Long {
        //todo kouknout na blockingget jestli nedela binec
        return purchaseRepo.saveTransaction(transaction, transactionId).blockingGet()
    }

    private fun actualizeTransactionPurchaseDatabase(transactionPurchases: List<TransactionPurchaseApiEntity>?, transactionId: Long): Completable {
        return Observable.fromIterable(transactionPurchases).flatMapCompletable { transactionPurchase ->
                Completable.fromSingle( purchaseRepo.saveTransactionPurchase(transactionPurchase, transactionId) )
            }
    }

    companion object {
        private val TAG = PurchaseFacadeImpl::class.java.simpleName
    }

}
