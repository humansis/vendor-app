package cz.quanti.android.vendor_app.repository.purchase.dto

data class Purchase(
    var products: MutableList<PurchasedProduct> = mutableListOf(),
    var vouchers: MutableList<Long> = mutableListOf(),
    var smartcard: String? = null,
    var beneficiaryId: Long? = null,
    var assistanceId: Long? = null,
    var createdAt: String = "",
    var vendorId: Long = 0,
    var dbId: Long = 0,
    var currency: String = "",
    var balanceBefore: Double? = null,
    var balanceAfter: Double? = null
)
