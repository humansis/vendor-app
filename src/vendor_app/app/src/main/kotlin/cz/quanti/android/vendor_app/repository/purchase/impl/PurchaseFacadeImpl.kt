package cz.quanti.android.vendor_app.repository.purchase.impl

import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseRepository
import cz.quanti.android.vendor_app.repository.purchase.dto.Invoice
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.Transaction
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
        Log.d(TAG, "Sync started" )
        return preparePurchases()
            .andThen(sendPurchasesToServer())
            .andThen(deleteSelectedProducts())
            .andThen(retrieveInvoices(vendorId))
            .andThen(retrieveTransactions(vendorId))

    }

    override fun unsyncedPurchases(): Single<List<Purchase>> {
        return purchaseRepo.getAllPurchases()
    }

    override fun getPurchasesCount(): Observable<Long> {
        return purchaseRepo.getPurchasesCount()
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
                            Log.d(
                                TAG,
                                "Voucher purchases sync finished successfully"
                            )
                            purchaseRepo.deleteAllVoucherPurchases().doOnComplete {
                                Log.d(
                                    TAG,
                                    "All voucher purchases successfully removed from db"
                                )
                            }
                        } else {
                            invalidPurchases.addAll(voucherPurchases)
                            Completable.complete()
                        }
                    }.andThen(
                        Observable.fromIterable(purchases.filter { it.smartcard != null })
                            .flatMapCompletable { purchase ->
                                purchaseRepo.sendCardPurchaseToServer(purchase)
                                    .flatMapCompletable { responseCode ->
                                        if (isPositiveResponseHttpCode(responseCode)) {
                                            purchaseRepo.deletePurchase(purchase).doOnComplete {
                                                Log.d(
                                                    TAG,
                                                    "Purchase ${purchase.dbId} by ${purchase.smartcard} successfully removed from db"
                                                )
                                            }
                                        } else {
                                            invalidPurchases.add(purchase)
                                            Completable.complete()
                                        }
                                    }
                            }
                            //throw exception after all purchases has been iterated
                            .doOnComplete {
                                Log.d(
                                    TAG,
                                    "Smartcard purchases sync finished successfully"
                                )
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

    private fun deleteSelectedProducts(): Completable {
        return purchaseRepo.deleteSelectedProducts()
    }

    private fun retrieveInvoices(vendorId: Int): Completable {
        return purchaseRepo.retrieveInvoices(vendorId).flatMapCompletable {
            val responseCode = it.first
            val invoicesList = it.second
            if (isPositiveResponseHttpCode(responseCode)) {
                actualizeInvoiceDatabase(invoicesList)
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
                var id: Long = 1
                deleteAllTransactions().andThen(
                    deleteAllTransactionPurchases().andThen(
                        Observable.fromIterable(transactionsList).flatMapCompletable { transactions ->
                            purchaseRepo.retrieveTransactionsPurchasesById(transactions.purchaseIds).flatMapCompletable { response ->
                                val transactionPurchasesList = response.second
                                if (isPositiveResponseHttpCode(response.first)) {
                                    saveTransactionToDb(transactions, id).flatMapCompletable { transactionId ->
                                        id++
                                        actualizeTransactionPurchaseDatabase(transactionPurchasesList, transactionId)
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
                    )
                )
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

    private fun saveTransactionToDb(transaction: TransactionApiEntity, transactionId: Long): Single<Long> {
        return purchaseRepo.saveTransaction(transaction, transactionId)
    }

    private fun deleteAllTransactionPurchases(): Completable {
        return purchaseRepo.deleteTransactionPurchases()
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
