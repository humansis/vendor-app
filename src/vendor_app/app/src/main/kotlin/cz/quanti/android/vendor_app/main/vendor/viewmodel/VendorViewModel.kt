package cz.quanti.android.vendor_app.main.vendor.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.main.vendor.adapter.ShoppingCartAdapter
import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.SelectedProduct
import cz.quanti.android.vendor_app.repository.facade.CommonFacade
import io.reactivex.Completable
import io.reactivex.Single

class VendorViewModel(private val facade: CommonFacade): ViewModel() {

    fun getProducts(): Single<List<Product>> {
        return facade.getProducts()
    }

    fun synchronizeWithServer(): Completable {
        return actualizeProducts()
    }

    fun getFirstCurrencies(): List<String> {
        return listOf("USD", "SYP", "EUR")
    }

    private fun actualizeProducts(): Completable {
        return facade.reloadProductFromServer()
    }
}
