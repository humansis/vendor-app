package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class V2TransactionsApiEntity (
    var totalCount: Int = 0,
    var data: List<TransactionsApiEntity> = listOf()
)
