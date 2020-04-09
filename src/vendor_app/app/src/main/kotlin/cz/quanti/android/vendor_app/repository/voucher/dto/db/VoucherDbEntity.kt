package cz.quanti.android.vendor_app.repository.voucher.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.utils.wrapper.ProductIdListWrapper
import java.util.*

@Entity(tableName = VendorDb.TABLE_VOUCHER)
data class VoucherDbEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0,
    var qrCode: String = "",
    var vendorId: String = "",
    var productIds: ProductIdListWrapper = ProductIdListWrapper(),
    var price: Double = 0.0,
    var currency: String = "",
    var value: Long = 0,
    var booklet: String = "",
    var usedAt: Date = Date()
)
