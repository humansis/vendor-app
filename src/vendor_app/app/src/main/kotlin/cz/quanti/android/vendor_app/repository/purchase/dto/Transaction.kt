package cz.quanti.android.vendor_app.repository.purchase.dto

data class Transaction(
    var value: Long = 0,
    var currency: String = "",
    var beneficiaryId: Long = 0,
    var createdAt: String = ""
)
