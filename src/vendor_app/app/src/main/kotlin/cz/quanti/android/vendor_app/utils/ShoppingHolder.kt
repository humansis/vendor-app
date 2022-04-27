package cz.quanti.android.vendor_app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ShoppingHolder(
    val cart: MutableList<SelectedProduct> = mutableListOf(),
    val currency: BehaviorSubject<String> = BehaviorSubject.createDefault("")
) : KoinComponent {
    private val purchaseFacade: PurchaseFacade by inject()

    fun addProduct(product: SelectedProduct): Completable {
        return purchaseFacade.addProductToCart(product)
    }

    fun getProductsSingle(): Single<List<SelectedProduct>> {
        return purchaseFacade.getProductsFromCartSingle()
    }

    fun getProductsLD(): LiveData<List<SelectedProduct>> {
        return purchaseFacade.getProductsFromCartObservable()
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
    }

    fun updateProduct(product: SelectedProduct): Completable {
        return purchaseFacade.updateProductInCart(product)
    }

    fun removeProduct(product: SelectedProduct): Completable {
        return purchaseFacade.removeProductFromCart(product)
    }

    fun removeProductsByType(typesToRemove: Set<Int>): Completable {
        return purchaseFacade.getProductsFromCartSingle().flatMapCompletable { selectedProducts ->
            Observable.fromIterable(selectedProducts).flatMapCompletable { selectedProduct ->
                if (typesToRemove.contains(selectedProduct.product.category.type.typeId)) {
                    purchaseFacade.removeProductFromCart(selectedProduct)
                } else {
                    Completable.complete()
                }
            }
        }
    }

    fun removeAllProducts(): Completable {
        return purchaseFacade.deleteAllProductsInCart()
    }
}
