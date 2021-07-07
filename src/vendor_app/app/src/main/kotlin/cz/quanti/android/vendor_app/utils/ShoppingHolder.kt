package cz.quanti.android.vendor_app.utils

import androidx.lifecycle.MutableLiveData
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct

data class ShoppingHolder(
    val cart: MutableList<SelectedProduct> = mutableListOf(),
    val vouchers: MutableList<Voucher> = mutableListOf(),
    val chosenCurrency: MutableLiveData<String> = MutableLiveData(""),
    var lastCurrencySelection: String = ""
)
