package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class TransactionPurchaseApiEntity(
    var id: Long = 0,
    var value: Double = 0.0,
    var currency: String = "",
    var beneficiaryId: Long = 0,
    var dateOfPurchase: String = ""
)
