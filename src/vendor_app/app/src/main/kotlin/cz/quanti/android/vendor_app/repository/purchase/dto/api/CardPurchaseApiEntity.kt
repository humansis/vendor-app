package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class CardPurchaseApiEntity(
    var products: List<PurchasedProductApiEntity> = listOf(),
    var createdAt: String = "",
    var vendorId: Long = 0,
    var beneficiaryId: Long? = null
)
