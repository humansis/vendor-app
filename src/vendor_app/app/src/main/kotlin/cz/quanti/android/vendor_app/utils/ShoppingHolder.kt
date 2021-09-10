package cz.quanti.android.vendor_app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ShoppingHolder(
    val cart: MutableList<SelectedProduct> = mutableListOf(),
    val vouchers: MutableList<Voucher> = mutableListOf(),
    val chosenCurrency: MutableLiveData<String> = MutableLiveData("")
) : KoinComponent {
    private val purchaseFacade: PurchaseFacade by inject()

    fun addProduct(product: SelectedProduct) {
        CoroutineScope(Dispatchers.IO).launch {
            purchaseFacade.addProductToCart(product)
        }
    }

    fun getProductsSingle(): Single<List<SelectedProduct>> {
        return purchaseFacade.getProductsFromCartSingle()
    }

    fun getProductsLD(): LiveData<List<SelectedProduct>> {
        return purchaseFacade.getProductsFromCartObservable()
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
    }

    fun updateProduct(product: SelectedProduct) {
        CoroutineScope(Dispatchers.IO).launch {
            purchaseFacade.updateProductInCart(product)
        }
    }

    fun removeProductAt(product: SelectedProduct) {
        CoroutineScope(Dispatchers.IO).launch {
            purchaseFacade.removeProductFromCartAt(product)
        }
    }

    fun removeAllProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            purchaseFacade.deleteAllProductsInCart()
        }
    }
}
