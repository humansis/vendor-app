package cz.quanti.android.vendor_app.repository.voucher.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.voucher.dto.db.VoucherPurchaseDbEntity
import io.reactivex.Single

@Dao
interface VoucherPurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(purchase: VoucherPurchaseDbEntity): Single<Long>

    @Delete
    fun delete(purchase: VoucherPurchaseDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_VOUCHER_PURCHASE)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_VOUCHER_PURCHASE)
    fun getAll(): Single<List<VoucherPurchaseDbEntity>>
}
