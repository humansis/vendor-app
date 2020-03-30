package cz.quanti.android.vendor_app.repository.voucher.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.voucher.dto.db.VoucherDbEntity
import io.reactivex.Single

@Dao
interface VoucherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(voucher: VoucherDbEntity)

    @Delete
    fun delete(voucher: VoucherDbEntity)

    @Query("SELECT * FROM " + VendorDb.TABLE_VOUCHER)
    fun getAll(): Single<List<VoucherDbEntity>>
}
