package cz.quanti.android.vendor_app.repository.common

import cz.quanti.android.vendor_app.repository.login.dto.Salt
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.product.dto.Product
import io.reactivex.Single
import retrofit2.Response

interface VendorServerApiRepository {
    fun getSalt(username: String): Single<Response<Salt>>

    fun login(vendor: Vendor): Single<Response<Vendor>>

    fun getProducts(): Single<Response<List<Product>>>
}
