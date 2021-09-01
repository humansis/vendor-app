package cz.quanti.android.vendor_app.repository.transaction.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.transaction.TransactionRepository
import cz.quanti.android.vendor_app.repository.transaction.dao.TransactionDao
import cz.quanti.android.vendor_app.repository.transaction.dao.TransactionPurchaseDao
import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.repository.transaction.dto.TransactionPurchase
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionApiEntity
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionPurchaseApiEntity
import cz.quanti.android.vendor_app.repository.transaction.dto.db.TransactionDbEntity
import cz.quanti.android.vendor_app.repository.transaction.dto.db.TransactionPurchaseDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val transactionPurchaseDao: TransactionPurchaseDao,
    private val api: VendorAPI
) : TransactionRepository {

    override fun getTransactions(): Single<List<Transaction>> {
        return transactionDao.getAll().flatMap { transactionsDb ->
            Observable.fromIterable(transactionsDb)
                .flatMapSingle { transactionDb ->
                    getTransactionPurchasesById(
                        getTransactionPurchaseIdsForTransaction(transactionDb.dbId)
                    ).flatMap { transactionPurchases ->
                        val transaction = Transaction(
                            // todo dodelat api request na endpoint aby se misto cisla projektu ukazoval jeho nazev
                            projectId = transactionDb.projectId,
                            purchases = transactionPurchases,
                            value = transactionDb.value,
                            currency = transactionDb.currency
                        )
                        Single.just(transaction)
                    }
                }.toList()
        }
    }

    private fun getTransactionPurchaseIdsForTransaction(transactionId: Long): List<Long> {
        val transactionPurchaseIds = mutableListOf<Long>()
        transactionPurchaseDao.getTransactionPurchaseForTransaction(transactionId).forEach {
            transactionPurchaseIds.add(it.dbId)
        }
        return transactionPurchaseIds
    }

    private fun getTransactionPurchasesById(purchaseIds: List<Long>): Single<List<TransactionPurchase>> {
        val transactionPurchases = mutableListOf<TransactionPurchase>()
        purchaseIds.forEach {
            val transactionPurchaseDb = transactionPurchaseDao.getTransactionPurchasesById(it)
            transactionPurchases.add(
                TransactionPurchase(
                    purchaseId = transactionPurchaseDb.dbId,
                    transactionId = transactionPurchaseDb.transactionId,
                    beneficiaryId = transactionPurchaseDb.beneficiaryId,
                    createdAt = transactionPurchaseDb.createdAt,
                    value = transactionPurchaseDb.value,
                    currency = transactionPurchaseDb.currency
                )
            )
        }
        return Single.just(transactionPurchases)
    }

    override fun retrieveTransactions(vendorId: Int): Single<Pair<Int, List<TransactionApiEntity>>> {
        return api.getTransactions(vendorId).map { response ->
            var transactions = listOf<TransactionApiEntity>()
            response.body()?.let { transactions = it }
            Pair(response.code(), transactions)
        }
    }

    override fun deleteTransactions(): Completable {
        return Completable.fromCallable { transactionDao.deleteAll() }
    }

    override fun saveTransaction(transaction: TransactionApiEntity, transactionId: Long): Single<Long> {
        return Single.fromCallable { transactionDao.insert(convertToDb(transaction, transactionId)) }
    }

    override fun retrieveTransactionsPurchases(
        vendorId: Int,
        projectId: Long,
        currency: String
    ): Single<Pair<Int, List<TransactionPurchaseApiEntity>>> {
        return api.getTransactionsPurchases(vendorId, projectId, currency).map { response ->
            var transactionPurchases = listOf<TransactionPurchaseApiEntity>()
            response.body()?.let { transactionPurchases = it }
            Pair(response.code(), transactionPurchases)
        }
    }

    override fun deleteTransactionPurchases(): Completable {
        return Completable.fromCallable { transactionPurchaseDao.deleteAll() }
    }

    override fun saveTransactionPurchase(transactionPurchase: TransactionPurchaseApiEntity, transactionId: Long): Single<Long> {
        return Single.fromCallable { transactionPurchaseDao.insert(convertToDb(transactionPurchase, transactionId)) }
    }

    private fun convertToDb(transaction: TransactionApiEntity, transactionId: Long): TransactionDbEntity {
        return TransactionDbEntity(
            dbId = transactionId,
            projectId = transaction.projectId,
            value = transaction.value,
            currency = transaction.currency
        )
    }

    private fun convertToDb(transactionPurchase: TransactionPurchaseApiEntity, transactionId: Long): TransactionPurchaseDbEntity {
        return TransactionPurchaseDbEntity(
            dbId = transactionPurchase.id,
            value = transactionPurchase.value,
            currency = transactionPurchase.currency,
            beneficiaryId = transactionPurchase.beneficiaryId,
            createdAt = transactionPurchase.dateOfPurchase,
            transactionId = transactionId
        )
    }
}
