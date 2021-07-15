package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class VoucherPurchaseApiEntity(
    var products: List<PurchasedProductApiEntity> = listOf(),
    var vouchers: List<Long> = listOf(),
    var vendorId: Long = 0,
    var createdAt: String = ""
)
