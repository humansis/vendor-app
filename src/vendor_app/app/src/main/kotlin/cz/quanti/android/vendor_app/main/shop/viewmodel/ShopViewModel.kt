package cz.quanti.android.vendor_app.main.shop.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import cz.quanti.android.vendor_app.utils.getDefaultCurrency
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class ShopViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val productFacade: ProductFacade,
    private val currentVendor: CurrentVendor,
    private val synchronizationManager: SynchronizationManager,
    private val preferences: AppPreferences
) : ViewModel() {

    fun syncStateObservable(): Observable<SynchronizationState> {
        return synchronizationManager.syncStateObservable()
    }

    fun getProducts(): Observable<List<Product>> {
        return productFacade.getProducts()
    }

    fun deleteProducts(): Completable {
        return productFacade.deleteProducts()
    }

    fun getSelectedProducts(): Single<List<SelectedProduct>> {
        return shoppingHolder.getProductsSingle()
    }

    fun getSelectedProductsLD(): LiveData<List<SelectedProduct>> {
        return shoppingHolder.getProductsLD()
    }

    fun setProducts(products: List<SelectedProduct>) {
        shoppingHolder.cart.clear()
        shoppingHolder.cart.addAll(products)
    }

    fun hasCashback(): SelectedProduct? {
        return shoppingHolder.cart.find { it.product.category.type == CategoryType.CASHBACK }
    }

    fun getTotal(): Double {
        return shoppingHolder.cart.sumOf { it.price }
    }

    fun removeSelectedProduct(product: SelectedProduct): Completable {
        return shoppingHolder.removeProduct(product)
    }

    fun emptyCart(): Completable {
        return shoppingHolder.removeAllProducts()
    }

    fun addToShoppingCart(product: SelectedProduct): Completable {
        return shoppingHolder.addProduct(product)
    }

    fun getCurrencies(): List<String> {
        return listOf("USD", "EUR", "SYP", "KHR", "UAH", "AMD", "MNT", "ETB", "ZMW")
    }

    fun getCurrency(): String? {
        return shoppingHolder.currency.value
    }

    fun getCurrencyObservable(): Observable<String> {
        if (shoppingHolder.currency.value == "") {
            val savedCurrency = preferences.currency
            if (savedCurrency.isNotEmpty()) {
                shoppingHolder.currency.onNext(savedCurrency)
            } else {
                setCurrency(
                    getDefaultCurrency(currentVendor.vendor.country)
                        .takeIf { it.isNotEmpty() } ?: "USD"
                )
            }
        }
        return shoppingHolder.currency
    }

    fun setCurrency(currency: String) {
        preferences.currency = currency
        shoppingHolder.currency.onNext(currency)
    }
}
