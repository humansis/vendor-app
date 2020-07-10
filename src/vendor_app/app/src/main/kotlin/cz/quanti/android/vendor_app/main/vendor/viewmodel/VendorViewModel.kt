package cz.quanti.android.vendor_app.main.vendor.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import io.reactivex.Completable
import io.reactivex.Single

class VendorViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val productFacade: ProductFacade,
    private val syncFacade: SynchronizationFacade,
    private val preferences: AppPreferences
) : ViewModel() {

    fun getLastCurrencySelection(): Int {
        return shoppingHolder.lastCurrencySelection
    }

    fun setLastCurrencySelection(selected: Int) {
        shoppingHolder.lastCurrencySelection = selected
    }

    fun getProducts(): Single<List<Product>> {
        return productFacade.getProducts()
    }

    fun synchronizeWithServer(): Completable {
        return syncFacade.synchronize()
    }

    fun getFirstCurrencies(): List<String> {
        return listOf("USD", "EUR", "SYP", "KHR", "UAH")
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

    fun getCurrency(): String {
        return shoppingHolder.chosenCurrency
    }

    fun setCurrency(currency: String) {
        shoppingHolder.chosenCurrency = currency
    }

    fun getLastSynced(): Long {
        return preferences.lastSynced
    }

    fun setLastSynced(time: Long) {
        preferences.lastSynced = time
    }
}
