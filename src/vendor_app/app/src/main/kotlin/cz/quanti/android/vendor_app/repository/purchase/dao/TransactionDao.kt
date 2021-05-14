package cz.quanti.android.vendor_app.repository.purchase.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.purchase.dto.db.TransactionDbEntity
import io.reactivex.Single

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(transaction: TransactionDbEntity): Long

    @Delete
    fun delete(transaction: TransactionDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_TRANSACTION_BATCH)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_TRANSACTION_BATCH)
    fun getAll(): Single<List<TransactionDbEntity>>

    @Query("SELECT count(*) FROM " + VendorDb.TABLE_TRANSACTION_BATCH)
    fun getCount(): Single<Int>
}
