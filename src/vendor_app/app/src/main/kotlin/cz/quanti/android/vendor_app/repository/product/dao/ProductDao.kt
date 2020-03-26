package cz.quanti.android.vendor_app.repository.product.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import io.reactivex.Single

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: ProductDbEntity)

    @Delete
    fun delete(product: ProductDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_PRODUCT)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_PRODUCT)
    fun getAll(): Single<List<ProductDbEntity>>
}
