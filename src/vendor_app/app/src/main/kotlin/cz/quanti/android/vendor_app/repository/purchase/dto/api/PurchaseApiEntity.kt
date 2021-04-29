package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class PurchaseApiEntity(
    var value: Long = 0,
    var currency: String = "",
    var beneficiaryId: Long = 0,
    var createdAt: String = ""
)
