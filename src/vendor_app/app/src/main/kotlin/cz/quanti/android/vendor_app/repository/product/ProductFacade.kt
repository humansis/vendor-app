package cz.quanti.android.vendor_app.repository.product

import cz.quanti.android.vendor_app.repository.product.dto.Product
import io.reactivex.Completable
import io.reactivex.Observable

interface ProductFacade {

    fun getProducts(): Observable<List<Product>>

    fun syncWithServer(): Completable
}
