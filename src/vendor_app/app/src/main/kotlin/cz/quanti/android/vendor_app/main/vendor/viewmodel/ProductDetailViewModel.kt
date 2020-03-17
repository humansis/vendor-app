package cz.quanti.android.vendor_app.main.vendor.viewmodel

import androidx.lifecycle.ViewModel

class ProductDetailViewModel: ViewModel() {

    fun getSupportedCurrencies(): List<String> {
        return listOf("USD", "SYP", "EUR")
    }
}
