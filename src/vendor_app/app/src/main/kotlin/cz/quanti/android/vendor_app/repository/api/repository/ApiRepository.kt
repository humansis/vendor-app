package cz.quanti.android.vendor_app.repository.api.repository

import cz.quanti.android.vendor_app.repository.api.response.ProductsResponse
import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.Salt
import cz.quanti.android.vendor_app.repository.entity.Vendor
import io.reactivex.Single
import retrofit2.Response

interface ApiRepository {
    fun getSalt(username: String): Single<Response<Salt>>

    fun login(vendor: Vendor): Single<Response<Vendor>>

    fun getProducts(): Single<Response<List<Product>>>
}
