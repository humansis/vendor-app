package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher

data class ShoppingHolder(
    val cart: MutableList<SelectedProduct> = mutableListOf(),
    val vouchers: MutableList<Voucher> = mutableListOf()
)
