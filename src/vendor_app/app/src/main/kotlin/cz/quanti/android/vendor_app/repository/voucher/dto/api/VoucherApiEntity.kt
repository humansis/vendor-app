package cz.quanti.android.vendor_app.repository.voucher.dto.api

import java.util.*

data class VoucherApiEntity(
    var id: Long = 0,
    var booklet: String = "",
    var productId: Long = 0,
    var quantity: Double = 0.0,
    var usedAt: Date = Date(),
    var value: Double = 0.0,
    var vendorId: Long = 0
)
