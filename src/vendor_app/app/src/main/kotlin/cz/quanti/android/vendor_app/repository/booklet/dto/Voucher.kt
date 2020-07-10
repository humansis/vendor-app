package cz.quanti.android.vendor_app.repository.booklet.dto

data class Voucher(
    var id: Long = 0,
    var qrCode: String = "",
    var currency: String = "",
    var value: Long = 0,
    var booklet: String = "",
    var passwords: List<String> = listOf()
)
