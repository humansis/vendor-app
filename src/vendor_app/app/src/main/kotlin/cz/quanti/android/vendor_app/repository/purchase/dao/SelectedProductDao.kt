package cz.quanti.android.vendor_app.repository.purchase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.purchase.dto.db.SelectedProductDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface SelectedProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: SelectedProductDbEntity): Completable

    @Delete
    fun delete(product: SelectedProductDbEntity): Completable

    @Query("DELETE FROM " + VendorDb.TABLE_SELECTED_PRODUCT)
    fun deleteAll(): Completable

    @Query("UPDATE " + VendorDb.TABLE_SELECTED_PRODUCT + " SET value = :value WHERE dbId = :dbId")
    fun update(dbId: Long?, value: Double): Completable

    @Query("SELECT * FROM " + VendorDb.TABLE_SELECTED_PRODUCT)
    fun getAll(): Single<List<SelectedProductDbEntity>>

    @Query("SELECT * FROM " + VendorDb.TABLE_SELECTED_PRODUCT)
    fun getAllObservable(): Observable<List<SelectedProductDbEntity>>
}
