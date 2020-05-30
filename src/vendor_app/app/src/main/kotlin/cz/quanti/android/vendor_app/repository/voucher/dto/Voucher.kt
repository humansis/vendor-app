package cz.quanti.android.vendor_app.repository.voucher.dto

import java.util.*

data class Voucher(
    var id: Long = 0,
    var qrCode: String = "",
    var vendorId: Long = 0,
    var productId: Long = 0,
    var price: Double = 0.0,
    var quantity: Double = 0.0,
    var currency: String = "",
    var value: Long = 0,
    var booklet: String = "",
    var usedAt: Date = Date(),
    var passwords: List<String> = listOf()
)
