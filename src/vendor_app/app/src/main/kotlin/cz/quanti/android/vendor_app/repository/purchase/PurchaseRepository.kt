package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface PurchaseRepository {

    fun savePurchase(purchase: Purchase): Completable

    fun sendCardPurchaseToServer(purchase: Purchase): Single<Int>

    fun sendVoucherPurchasesToServer(purchases: List<Purchase>): Single<Int>

    fun getAllPurchases(): Single<List<Purchase>>

    fun deletePurchasedProducts(): Completable

    fun deletePurchase(purchase: Purchase): Completable

    fun deleteAllVoucherPurchases(): Completable

    fun getPurchasesCount(): Observable<Long>

    // --- cart---
    fun addProductToCart(product: SelectedProduct): Completable

    fun getProductsFromCartObservable(): Observable<List<SelectedProduct>>

    fun updateProductInCart(product: SelectedProduct): Completable

    fun removeProductFromCart(product: SelectedProduct): Completable

    fun deleteAllProductsInCart(): Completable
}
