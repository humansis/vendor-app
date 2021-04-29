package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class V2PurchaseApiEntity (
    var totalCount: Int = 0,
    var data: List<PurchaseApiEntity> = listOf()
)
