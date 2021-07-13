package cz.quanti.android.vendor_app.main.shop.viewmodel

import androidx.lifecycle.*
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

class ShopViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val productFacade: ProductFacade,
    private val syncFacade: SynchronizationFacade,
    private val preferences: AppPreferences,
    private val currentVendor: CurrentVendor,
    private val synchronizationManager: SynchronizationManager
) : ViewModel() {
    val cartSizeLD: MutableLiveData<Int> = MutableLiveData(shoppingHolder.cart.size)

    fun syncNeededObservable(): Observable<SynchronizationState> {
        return synchronizationManager.syncStateObservable()
            .filter { it == SynchronizationState.SUCCESS }
    }

    fun getProducts(): Observable<List<Product>> {
        return productFacade.getProducts()
    }

    fun getShoppingCart(): List<SelectedProduct> {
        return shoppingHolder.cart
    }

    fun addToShoppingCart(product: SelectedProduct) {
        shoppingHolder.cart.add(product)
        cartSizeLD.value = shoppingHolder.cart.size
    }

    fun removeFromCart(position: Int) {
        shoppingHolder.cart.removeAt(position)
        cartSizeLD.value = shoppingHolder.cart.size
    }

    fun clearCart() {
        shoppingHolder.cart.clear()
        cartSizeLD.value = shoppingHolder.cart.size
    }

    fun getCurrencies(): List<String> {
        return listOf("USD", "EUR", "SYP", "KHR", "UAH", "AMD", "MNT", "ETB", "ZMW")
    }

    fun getCurrency(): LiveData<String> {
        if(shoppingHolder.chosenCurrency.value == "") {
            setCurrency(getDefaultCurrency(currentVendor.vendor.country))
        }
        return shoppingHolder.chosenCurrency
    }

    fun setCurrency(currency: String) {
        shoppingHolder.chosenCurrency.postValue(currency)
    }
}
