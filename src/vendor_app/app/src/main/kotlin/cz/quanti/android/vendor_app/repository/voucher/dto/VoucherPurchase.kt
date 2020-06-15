package cz.quanti.android.vendor_app.repository.voucher.dto

import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct

data class VoucherPurchase(
    var products: MutableList<SelectedProduct> = mutableListOf(),
    var vouchers: MutableList<Long> = mutableListOf(),
    var vendorId: Long = 0,
    var createdAt: String = ""
)
