package cz.quanti.android.vendor_app.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.db.schema.VendorDb
import java.util.*

data class Voucher(
    var id: Long = 0,
    var qrCode: String = "",
    var vendorId: String = "",
    var productIds: Array<Long> = arrayOf(),
    var price: Long = 0,
    var currency: String = "",
    var value: Long = 0,
    var booklet: String = "",
    var usedAt: Date = Date()
) {
}
