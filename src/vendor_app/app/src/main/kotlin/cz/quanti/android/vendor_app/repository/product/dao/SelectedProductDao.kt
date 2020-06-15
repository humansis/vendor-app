package cz.quanti.android.vendor_app.repository.product.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.product.dto.db.SelectedProductDbEntity
import io.reactivex.Single

@Dao
interface SelectedProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: SelectedProductDbEntity): Single<Long>

    @Delete
    fun delete(product: SelectedProductDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_SELECTED_PRODUCT)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_SELECTED_PRODUCT)
    fun getAll(): Single<List<SelectedProductDbEntity>>

    @Query(
        "SELECT * FROM " + VendorDb.TABLE_SELECTED_PRODUCT
            + " WHERE purchaseId = :purchaseId"
    )
    fun getProductsForPurchase(purchaseId: Long): Single<List<SelectedProductDbEntity>>
}
