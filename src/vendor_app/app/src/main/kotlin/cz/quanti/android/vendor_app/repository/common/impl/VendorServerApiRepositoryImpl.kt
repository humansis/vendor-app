package cz.quanti.android.vendor_app.repository.common.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.common.VendorServerApiRepository
import cz.quanti.android.vendor_app.repository.login.dto.Salt
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.product.dto.Product
import io.reactivex.Single
import retrofit2.Response

class VendorServerApiRepositoryImpl(private val api: VendorAPI) :
    VendorServerApiRepository {
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
