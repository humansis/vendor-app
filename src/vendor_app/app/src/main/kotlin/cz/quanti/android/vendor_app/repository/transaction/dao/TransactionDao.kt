package cz.quanti.android.vendor_app.repository.transaction.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.transaction.dto.db.TransactionDbEntity
import io.reactivex.Observable
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
    fun getAll(): Observable<List<TransactionDbEntity>>

    @Query("SELECT count(*) FROM " + VendorDb.TABLE_TRANSACTION_BATCH)
    fun getCount(): Single<Int>
}
