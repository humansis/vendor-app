package cz.quanti.android.vendor_app.repository.purchase.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.purchase.dto.db.SelectedProductDbEntity
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface SelectedProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: SelectedProductDbEntity): Long

    @Delete
    fun delete(product: SelectedProductDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_SELECTED_PRODUCT)
    fun deleteAll()

    @Query("UPDATE " + VendorDb.TABLE_SELECTED_PRODUCT + " SET value = :value WHERE dbId = :dbId")
    fun update(dbId: Long?, value: Double)

    @Query("SELECT * FROM " + VendorDb.TABLE_SELECTED_PRODUCT)
    fun getAll(): List<SelectedProductDbEntity>

    @Query("SELECT * FROM " + VendorDb.TABLE_SELECTED_PRODUCT)
    fun getAllObservable(): Observable<List<SelectedProductDbEntity>>
}
