package cz.quanti.android.vendor_app.repository.api.repository.impl

import cz.quanti.android.vendor_app.repository.api.VendorAPI
import cz.quanti.android.vendor_app.repository.api.repository.ApiRepository
import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.Salt
import cz.quanti.android.vendor_app.repository.entity.Vendor
import io.reactivex.Single
import retrofit2.Response

class ApiRepositoryImpl(private val api: VendorAPI) : ApiRepository {
    override fun getSalt(username: String): Single<Response<Salt>> {
        return api.getSalt(username)
    }

    override fun login(vendor: Vendor): Single<Response<Vendor>> {
        return api.postLogin(vendor)
    }

    override fun getProducts(): Single<Response<List<Product>>> {
        return api.getProducts()
    }
}
