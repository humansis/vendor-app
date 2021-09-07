package cz.quanti.android.vendor_app.main.shop.viewmodel

import androidx.lifecycle.*
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.category.CategoryFacade
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import cz.quanti.android.vendor_app.utils.getDefaultCurrency
import io.reactivex.Observable

class ShopViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val categoryFacade: CategoryFacade,
    private val productFacade: ProductFacade,
    private val currentVendor: CurrentVendor,
    private val synchronizationManager: SynchronizationManager,
    private val preferences: AppPreferences
) : ViewModel() {

    fun syncStateObservable(): Observable<SynchronizationState> {
        return synchronizationManager.syncStateObservable()
    }

    fun getCategories(): Observable<List<Category>> {
        return categoryFacade.getCategories() // TODO finish
//        return Observable.just(listOf())
//        return Observable.just(listOf(
//            Category(
//                1,
//                "dry foods",
//                CategoryType.FOOD
//            ),
//            Category(
//                2,
//                "wet foods",
//                CategoryType.FOOD
//            ),
//            Category(
//                3,
//                "drugs",
//                CategoryType.NONFOOD
//            ),
//            Category(
//                4,
//                "tools",
//                CategoryType.NONFOOD
//            ),
//            Category(
//                5,
//                "cash",
//                CategoryType.CASHBACK
//            )
//        ))
    }

    fun getProducts(): Observable<List<Product>> {
        return productFacade.getProducts()
    }

    fun getSelectedProducts(): LiveData<List<SelectedProduct>> {
        return shoppingHolder.getProducts()
    }

    fun addToShoppingCart(product: SelectedProduct) {
        shoppingHolder.addProduct(product)
    }

    fun getCurrencies(): List<String> {
        return listOf("USD", "EUR", "SYP", "KHR", "UAH", "AMD", "MNT", "ETB", "ZMW")
    }

    fun getCurrency(): LiveData<String> {
        if (shoppingHolder.chosenCurrency.value == "") {
            val savedCurrency = preferences.currency
            if (savedCurrency.isNotEmpty()) {
                shoppingHolder.chosenCurrency.postValue(savedCurrency)
            } else {
                setCurrency(getDefaultCurrency(currentVendor.vendor.country))
            }
        }
        return shoppingHolder.chosenCurrency
    }

    fun setCurrency(currency: String) {
        preferences.currency = currency
        shoppingHolder.chosenCurrency.postValue(currency)
    }
}
