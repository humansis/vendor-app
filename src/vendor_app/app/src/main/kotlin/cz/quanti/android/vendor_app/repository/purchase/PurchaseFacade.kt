package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface PurchaseFacade {

    fun savePurchase(purchase: Purchase): Completable

    fun syncWithServer(): Completable

    fun getPurchasesCount(): Observable<Long>

    fun addProductToCart(product: SelectedProduct)

    fun getProductsFromCart(): Observable<List<SelectedProduct>>

    fun updateProductInCart(product: SelectedProduct)

    fun removeProductFromCartAt(product: SelectedProduct)

    fun deleteAllProductsInCart()
}
