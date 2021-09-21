package cz.quanti.android.vendor_app.repository.product.impl

import android.content.Context
import com.bumptech.glide.Glide
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.ProductRepository
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable

class ProductFacadeImpl(
    private val productRepo: ProductRepository,
    private val context: Context
) : ProductFacade {

    override fun getProducts(): Observable<List<Product>> {
        return productRepo.getProducts()
    }

    override fun syncWithServer(vendor: Vendor): Completable {
        return reloadProductFromServer(vendor)
    }

    override fun deleteProducts(): Completable {
        return productRepo.deleteProducts()
    }

    private fun reloadProductFromServer(vendor: Vendor): Completable {
        return productRepo.loadProductsFromServer(vendor).flatMapCompletable { response ->
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
            throw VendorAppException("Products returned from server were empty.")
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
