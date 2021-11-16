package cz.quanti.android.vendor_app.repository.transaction.impl

import cz.quanti.android.vendor_app.repository.transaction.TransactionFacade
import cz.quanti.android.vendor_app.repository.transaction.TransactionRepository
import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionApiEntity
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionPurchaseApiEntity
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class TransactionFacadeImpl(
    private val transactionRepo: TransactionRepository
) : TransactionFacade {

    override fun syncWithServer(vendorId: Int): Completable {
        return retrieveTransactions(vendorId)
    }

    override fun getTransactions(): Observable<List<Transaction>> {
        return transactionRepo.getTransactions()
    }

    override fun deleteTransactions(): Completable {
        return deleteAllTransactions().andThen(
            deleteAllTransactionPurchases()
        )
    }

    private fun retrieveTransactions(vendorId: Int): Completable {
        return transactionRepo.retrieveTransactions(vendorId).flatMapCompletable {
            val responseCode = it.first
            val transactionsList = it.second
            if (isPositiveResponseHttpCode(responseCode)) {
                var id: Long = 1
                deleteAllTransactions().andThen(
                    deleteAllTransactionPurchases().andThen(
                        Observable.fromIterable(transactionsList)
                            .flatMapCompletable { transactions ->
                                transactionRepo.retrieveTransactionsPurchases(
                                    vendorId,
                                    transactions.projectId,
                                    transactions.currency
                                ).flatMapCompletable { response ->
                                    val transactionPurchasesList = response.second
                                    if (isPositiveResponseHttpCode(response.first)) {
                                        saveTransactionToDb(
                                            transactions,
                                            id
                                        ).flatMapCompletable { transactionId ->
                                            id++
                                            actualizeTransactionPurchaseDatabase(
                                                transactionPurchasesList,
                                                transactionId
                                            )
                                        }
                                    } else {
                                        // todo doresit aby exceptiony neprerusovaly sync
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
                // todo doresit aby exceptiony neprerusovaly sync
                throw VendorAppException("Received code $responseCode when trying download transactions.").apply {
                    apiError = true
                    apiResponseCode = responseCode
                }
            }
        }
    }

    private fun deleteAllTransactions(): Completable {
        return transactionRepo.deleteTransactions()
    }

    private fun saveTransactionToDb(
        transaction: TransactionApiEntity,
        transactionId: Long
    ): Single<Long> {
        return transactionRepo.saveTransaction(transaction, transactionId)
    }

    private fun deleteAllTransactionPurchases(): Completable {
        return transactionRepo.deleteTransactionPurchases()
    }

    private fun actualizeTransactionPurchaseDatabase(
        transactionPurchases: List<TransactionPurchaseApiEntity>?,
        transactionId: Long
    ): Completable {
        return Observable.fromIterable(transactionPurchases)
            .flatMapCompletable { transactionPurchase ->
                Completable.fromSingle(
                    transactionRepo.saveTransactionPurchase(
                        transactionPurchase,
                        transactionId
                    )
                )
            }
    }
}
