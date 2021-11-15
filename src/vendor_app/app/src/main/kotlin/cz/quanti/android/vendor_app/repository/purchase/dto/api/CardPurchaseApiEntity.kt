package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class CardPurchaseApiEntity(
    var products: List<PurchasedProductApiEntity> = listOf(),
    var createdAt: String = "",
    var vendorId: Long = 0,
    var beneficiaryId: Long? = null,
    var distributionId: Long? = null,
    var balanceBefore: Double? = 0.0,
    var balanceAfter: Double? = 0.0
)
