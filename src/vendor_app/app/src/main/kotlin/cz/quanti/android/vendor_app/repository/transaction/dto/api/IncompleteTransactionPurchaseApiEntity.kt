package cz.quanti.android.vendor_app.repository.transaction.dto.api

data class IncompleteTransactionPurchaseApiEntity(
    var id: Long = 0,
    var value: Double = 0.0,
    var currency: String = "",
    var beneficiaryId: Long = 0,
    var dateOfPurchase: String = "",
    var reason: String = ""
)
