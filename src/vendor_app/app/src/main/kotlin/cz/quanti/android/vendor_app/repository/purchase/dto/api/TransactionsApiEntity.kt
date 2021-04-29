package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class TransactionsApiEntity(
    var projectId: Int = 0,
    var purchaseIds: List<Int> = listOf(),
    var value: Long = 0,
    var currency: String = ""
)

