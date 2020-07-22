package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class CardPurchaseApiEntity(
    var products: List<SelectedProductApiEntity> = listOf(),
    var vendorId: Long = 0,
    var createdAt: String = ""
)
