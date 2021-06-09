package cz.quanti.android.vendor_app.main.vendor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import cz.quanti.android.vendor_app.utils.getDefaultCurrency
import io.reactivex.Observable
import io.reactivex.Single

class VendorViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val productFacade: ProductFacade,
    private val syncFacade: SynchronizationFacade,
    private val preferences: AppPreferences,
    private val currentVendor: CurrentVendor,
    private val synchronizationManager: SynchronizationManager
) : ViewModel() {
    val cartSizeLD: MutableLiveData<Int> = MutableLiveData(0)

    fun getLastCurrencySelection(): String {
        if (shoppingHolder.lastCurrencySelection == "") {
            shoppingHolder.lastCurrencySelection = getDefaultCurrency(currentVendor.vendor.country)
        }
        return shoppingHolder.lastCurrencySelection
    }

    fun syncNeededObservable(): Observable<SynchronizationState> {
        return synchronizationManager.syncStateObservable()
            .filter { it == SynchronizationState.SUCCESS }
    }

    fun setLastCurrencySelection(selected: String) {
        shoppingHolder.lastCurrencySelection = selected
    }

    fun getProducts(): Single<List<Product>> {
        return productFacade.getProducts()
    }

    fun getFirstCurrencies(): List<String> {
        return listOf("USD", "EUR", "SYP", "KHR", "UAH", "AMD", "MNT", "ETB")
    }

    fun removeFromCart(position: Int) {
        shoppingHolder.cart.removeAt(position)
        cartSizeLD.value = shoppingHolder.cart.size
    }

    fun clearCart() {
        shoppingHolder.cart.clear()
        cartSizeLD.value = shoppingHolder.cart.size
    }

    fun addToShoppingCart(product: SelectedProduct) {
        shoppingHolder.cart.add(product)
        cartSizeLD.value = shoppingHolder.cart.size
    }

    fun getShoppingCart(): List<SelectedProduct> {
        return shoppingHolder.cart
    }

    fun getCurrency(): LiveData<String> {
        return shoppingHolder.chosenCurrency
    }

    fun setCurrency(currency: String) {
        shoppingHolder.chosenCurrency.postValue(currency)
    }
}
