package cz.quanti.android.vendor_app.repository.purchase.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.purchase.dto.db.PurchasedProductDbEntity
import io.reactivex.Single

@Dao
interface PurchasedProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: PurchasedProductDbEntity): Long

    @Delete
    fun delete(product: PurchasedProductDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_PURCHASED_PRODUCT)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_PURCHASED_PRODUCT)
    fun getAll(): Single<List<PurchasedProductDbEntity>>

    @Query(
        "SELECT * FROM " + VendorDb.TABLE_PURCHASED_PRODUCT +
            " WHERE purchaseId = :purchaseId"
    )
    fun getProductsForPurchase(purchaseId: Long): Single<List<PurchasedProductDbEntity>>
}
