package cz.quanti.android.vendor_app.repository.purchase.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.purchase.dto.db.PurchaseDbEntity
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(purchase: PurchaseDbEntity): Long

    @Delete
    fun delete(purchase: PurchaseDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_PURCHASE)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_PURCHASE)
    fun getAll(): Single<List<PurchaseDbEntity>>

    @Query("SELECT count(*) FROM " + VendorDb.TABLE_PURCHASE)
    fun getCount(): Observable<Long>
}
