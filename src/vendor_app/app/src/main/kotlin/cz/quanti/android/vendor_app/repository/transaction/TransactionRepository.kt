package cz.quanti.android.vendor_app.repository.transaction

import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionApiEntity
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionPurchaseApiEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface TransactionRepository {

    fun retrieveTransactions(vendorId: Int): Single<Pair<Int, List<TransactionApiEntity>>>

    fun deleteTransactions(): Completable

    fun saveTransaction(transaction: TransactionApiEntity, transactionId: Long): Single<Long>

    fun getTransactions(): Observable<List<Transaction>>

    fun retrieveTransactionsPurchases(
        vendorId: Int,
        projectId: Long,
        currency: String
    ): Single<Pair<Int, List<TransactionPurchaseApiEntity>>>

    fun deleteTransactionPurchases(): Completable

    fun saveTransactionPurchase(
        transactionPurchase: TransactionPurchaseApiEntity,
        transactionId: Long
    ): Single<Long>
}
