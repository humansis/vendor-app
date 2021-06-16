package cz.quanti.android.vendor_app.repository.product

import cz.quanti.android.vendor_app.repository.product.dto.Product
import io.reactivex.Completable
import io.reactivex.Single

interface ProductFacade {

    fun getProducts(): Single<List<Product>>

    fun syncWithServer(): Completable
}
