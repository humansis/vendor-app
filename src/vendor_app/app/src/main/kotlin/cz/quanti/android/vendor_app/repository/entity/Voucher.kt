package cz.quanti.android.vendor_app.repository.entity

import java.util.Date

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
)
