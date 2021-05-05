package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class V2TransactionsApiEntity (
    var totalCount: Int = 0,
    var data: List<TransactionApiEntity> = listOf()
)

data class TransactionApiEntity(
    var projectId: Long = 0,
    var purchaseIds: List<Int> = listOf(),
    var value: Double = 0.0,
    var currency: String = ""
)