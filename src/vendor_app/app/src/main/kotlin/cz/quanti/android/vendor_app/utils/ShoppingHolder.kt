package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.main.checkout.CheckoutScreenState
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct

data class ShoppingHolder(
    val cart: MutableList<SelectedProduct> = mutableListOf(),
    val vouchers: MutableList<Voucher> = mutableListOf(),
    var chosenCurrency: String = "",
    var lastCurrencySelection: String = ""
)
