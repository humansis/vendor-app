package cz.quanti.android.vendor_app.repository.voucher.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb
import java.util.*

@Entity(tableName = VendorDb.TABLE_VOUCHER)
data class VoucherDbEntity(
    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0,
    var id: Long = 0,
    var booklet: String = "",
    var productId: Long = 0,
    var quantity: Double = 0.0,
    var usedAt: Date = Date(),
    var value: Double = 0.0,
    var vendorId: Long = 0
)
