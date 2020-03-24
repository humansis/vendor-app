package cz.quanti.android.vendor_app.repository.db.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.db.entity.ProductDbEntity
import cz.quanti.android.vendor_app.repository.db.schema.VendorDb
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
