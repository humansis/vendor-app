package cz.quanti.android.vendor_app.main.vendor.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import io.reactivex.Completable
import io.reactivex.Single

class VendorViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val productFacade: ProductFacade,
    private val voucherFacade: VoucherFacade,
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
        return voucherFacade.syncWithServer()
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
