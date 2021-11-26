package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface PurchaseFacade {

    fun savePurchase(purchase: Purchase): Completable

    fun syncWithServer(): Completable

    fun getSyncSubject(): PublishSubject<SynchronizationSubject>

    fun getPurchasesCount(): Observable<Long>

    fun addProductToCart(product: SelectedProduct): Completable

    fun getProductsFromCartSingle(): Single<List<SelectedProduct>>

    fun getProductsFromCartObservable(): Observable<List<SelectedProduct>>

    fun updateProductInCart(product: SelectedProduct): Completable

    fun removeProductFromCart(product: SelectedProduct): Completable

    fun deleteAllProductsInCart(): Completable
}
