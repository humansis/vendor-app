package cz.quanti.android.vendor_app.main.vendor.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import io.reactivex.Completable
import io.reactivex.Single

class VendorViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val productFacade: ProductFacade
) : ViewModel() {

    fun getProducts(): Single<List<Product>> {
        return productFacade.getProducts()
    }

    fun synchronizeWithServer(): Completable {
        return actualizeProducts()
    }

    fun getFirstCurrencies(): List<String> {
        return listOf("USD", "SYP", "EUR")
    }

    fun removeFromCart(position: Int) {
        shoppingHolder.cart.removeAt(position)
    }

    fun clearCart() {
        shoppingHolder.cart.clear()
    }

    fun addToShoppingCart(product: SelectedProduct) {
        shoppingHolder.cart.add(product)
    }

    fun getShoppingCart(): List<SelectedProduct> {
        return shoppingHolder.cart
    }

    fun setVendorState(state: Int) {
        shoppingHolder.vendorScreenState = state
    }

    fun getVendorState(): Int {
        return shoppingHolder.vendorScreenState
    }

    private fun actualizeProducts(): Completable {
        return productFacade.reloadProductFromServer()
    }
}
