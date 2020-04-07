package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.main.checkout.CheckoutScreenState
import cz.quanti.android.vendor_app.main.vendor.VendorScreenState
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher

data class ShoppingHolder(
    val cart: MutableList<SelectedProduct> = mutableListOf(),
    val vouchers: MutableList<Voucher> = mutableListOf(),
    var vendorScreenState: VendorScreenState = VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED,
    var checkoutScreenState: CheckoutScreenState = CheckoutScreenState.STATE_PAYMENT_SHOWED,
    var chosenCurrency: String = ""
)
