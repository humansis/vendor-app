package cz.quanti.android.vendor_app.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import cz.quanti.android.vendor_app.repository.db.entity.VoucherDbEntity

@Dao
interface VoucherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(voucher: VoucherDbEntity)

    @Delete
    fun delete(voucher: VoucherDbEntity)
}
