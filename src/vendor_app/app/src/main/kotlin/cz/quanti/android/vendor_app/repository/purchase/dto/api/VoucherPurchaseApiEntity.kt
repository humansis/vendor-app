package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class VoucherPurchaseApiEntity(
    var products: List<SelectedProductApiEntity> = listOf(),
    var vouchers: List<Long> = listOf(),
    var vendorId: Long = 0,
    var createdAt: String = ""
)
