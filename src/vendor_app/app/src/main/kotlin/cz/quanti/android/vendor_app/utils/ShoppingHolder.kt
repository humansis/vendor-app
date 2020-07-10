package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.main.checkout.CheckoutScreenState
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct

data class ShoppingHolder(
    val cart: MutableList<SelectedProduct> = mutableListOf(),
    val vouchers: MutableList<Voucher> = mutableListOf(),
    var checkoutScreenState: CheckoutScreenState = CheckoutScreenState.STATE_PAYMENT_SHOWED,
    var chosenCurrency: String = "",
    var lastCurrencySelection: Int = 0
)
