package cz.quanti.android.vendor_app.repository.voucher.dto.api

import com.google.gson.annotations.SerializedName

data class VoucherApiEntity(
    var id: Long = 0,
    var qrCode: String = "",
    var vendorId: String = "",
    var productIds: Array<Long> = arrayOf(),
    var price: Double = 0.0,
    var currency: String = "",
    var value: Long = 0,
    var booklet: String = "",
    @SerializedName("used_at")
    var usedAt: String = ""
)
