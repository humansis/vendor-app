package cz.quanti.android.vendor_app.repository.product.impl

import android.content.Context
import com.bumptech.glide.Glide
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.ProductRepository
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import quanti.com.kotlinlog.Log

class ProductFacadeImpl(
    private val productRepo: ProductRepository,
    private val context: Context
) : ProductFacade {

    override fun getProducts(): Observable<List<Product>> {
        return productRepo.getProducts()
    }

    override fun syncWithServer(
        syncSubjectReplaySubject: ReplaySubject<SynchronizationSubject>,
        vendorId: Int
    ): Completable {
        return Completable.fromCallable {
            syncSubjectReplaySubject.onNext(SynchronizationSubject.PRODUCTS_DOWNLOAD)
        }.andThen(reloadProductFromServer(vendorId))
    }

    override fun deleteProducts(): Completable {
        return productRepo.deleteProducts()
    }

    private fun reloadProductFromServer(
        vendorId: Int
    ): Completable {
        return productRepo.loadProductsFromServer(vendorId).flatMapCompletable { response ->
            val responseCode = response.first
            val products = response.second.toMutableList()
            if (isPositiveResponseHttpCode(responseCode)) {
                actualizeDatabase(products)
            } else {
                Completable.error(
                    VendorAppException(
                        "Could not get products from server."
                    ).apply {
                        apiError = true
                        apiResponseCode = responseCode
                    })
            }
        }
    }

    private fun actualizeDatabase(products: List<Product>?): Completable {
        return if (products == null) {
            Log.d("Products returned from server were empty.")
            Completable.complete()
        } else {
            productRepo.deleteProducts().andThen(
                Observable.fromIterable(products).flatMapCompletable { product ->
                    productRepo.saveProduct(product)
                        .andThen(Completable.fromCallable {
                            Glide.with(context).load(product.image)
                        })
                })
        }
    }
}
