package cz.quanti.android.vendor_app.repository.transaction.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.transaction.dto.db.TransactionPurchaseDbEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TransactionPurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(transactionPurchase: TransactionPurchaseDbEntity): Single<Long>

    @Delete
    fun delete(transactionPurchase: TransactionPurchaseDbEntity): Completable

    @Query("DELETE FROM " + VendorDb.TABLE_TRANSACTION_PURCHASE)
    fun deleteAll(): Completable

    @Query("SELECT * FROM " + VendorDb.TABLE_TRANSACTION_PURCHASE)
    fun getAll(): Single<List<TransactionPurchaseDbEntity>>

    @Query(
        "SELECT * FROM " + VendorDb.TABLE_TRANSACTION_PURCHASE +
            " WHERE transactionId = :transactionId"
    )
    fun getTransactionPurchaseForTransaction(transactionId: Long): List<TransactionPurchaseDbEntity>

    @Query(
        "SELECT * FROM " + VendorDb.TABLE_TRANSACTION_PURCHASE +
            " WHERE dbId = :dbId"
    )
    fun getTransactionPurchasesById(dbId: Long): TransactionPurchaseDbEntity

    @Query("SELECT count(*) FROM " + VendorDb.TABLE_TRANSACTION_PURCHASE)
    fun getCount(): Single<Int>
}
