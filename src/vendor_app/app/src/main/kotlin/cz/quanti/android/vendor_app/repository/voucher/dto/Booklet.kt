package cz.quanti.android.vendor_app.repository.voucher.dto

data class Booklet(
    var id: Long = 0,
    var code: String = "",
    var password: String = "",
    var state: Int = STATE_NORMAL
) {
    companion object {
        const val STATE_NORMAL = 0
        const val STATE_PROTECTED = 1
        const val STATE_DEACTIVATED = 2
    }
}
