package cz.quanti.android.vendor_app.repository.purchase.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.purchase.dto.db.TransactionPurchaseDbEntity
import io.reactivex.Single

@Dao
interface TransactionPurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(transactionPurchase: TransactionPurchaseDbEntity): Long

    @Delete
    fun delete(transactionPurchase: TransactionPurchaseDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_TRANSACTION_PURCHASE)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_TRANSACTION_PURCHASE)
    fun getAll(): Single<List<TransactionPurchaseDbEntity>>

    @Query("SELECT count(*) FROM " + VendorDb.TABLE_TRANSACTION_PURCHASE)
    fun getCount(): Single<Int>
}
