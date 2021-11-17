package cz.quanti.android.vendor_app.repository.product.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: ProductDbEntity): Completable

    @Delete
    fun delete(product: ProductDbEntity): Completable

    @Query("DELETE FROM " + VendorDb.TABLE_PRODUCT)
    fun deleteAll(): Completable

    @Query("SELECT * FROM " + VendorDb.TABLE_PRODUCT)
    fun getAll(): Observable<List<ProductDbEntity>>

    @Query("SELECT * FROM " + VendorDb.TABLE_PRODUCT + " WHERE id = :productId")
    fun getProductById(productId: Long): ProductDbEntity
}
