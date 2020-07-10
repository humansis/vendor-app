package cz.quanti.android.vendor_app.repository.purchase.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.purchase.dto.db.VoucherPurchaseDbEntity
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface VoucherPurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(voucherPurchase: VoucherPurchaseDbEntity): Long

    @Delete
    fun delete(voucherPurchase: VoucherPurchaseDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_VOUCHER_PURCHASE)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_VOUCHER_PURCHASE)
    fun getAll(): Single<List<VoucherPurchaseDbEntity>>

    @Query(
        "SELECT * FROM " + VendorDb.TABLE_VOUCHER_PURCHASE
            + " WHERE purchaseId = :purchaseId"
    )
    fun getVouchersForPurchase(purchaseId: Long): Maybe<List<VoucherPurchaseDbEntity>>
}
