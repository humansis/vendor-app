package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class V2TransactionPurchaseApiEntity (
    var totalCount: Int = 0,
    var data: List<TransactionPurchaseApiEntity> = listOf()
)

data class TransactionPurchaseApiEntity(
    var value: Long = 0,
    var currency: String = "",
    var beneficiaryId: Long = 0,
    var createdAt: String = ""
)
