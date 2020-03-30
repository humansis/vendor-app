package cz.quanti.android.vendor_app.main.vendor.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.dto.Product
import io.reactivex.Completable
import io.reactivex.Single

class VendorViewModel(private val productFacade: ProductFacade) : ViewModel() {

    fun getProducts(): Single<List<Product>> {
        return productFacade.getProducts()
    }

    fun synchronizeWithServer(): Completable {
        return actualizeProducts()
    }

    fun getFirstCurrencies(): List<String> {
        return listOf("USD", "SYP", "EUR")
    }

    private fun actualizeProducts(): Completable {
        return productFacade.reloadProductFromServer()
    }
}
