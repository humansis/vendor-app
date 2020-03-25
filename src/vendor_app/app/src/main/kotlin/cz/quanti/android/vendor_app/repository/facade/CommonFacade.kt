package cz.quanti.android.vendor_app.repository.facade

import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.Vendor
import cz.quanti.android.vendor_app.repository.entity.Voucher
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response

interface CommonFacade {
    fun login(username: String, password: String): Single<Response<Vendor>>

    fun reloadProductFromServer(): Completable

    fun getProducts(): Single<List<Product>>

    fun saveVouchers(vouchers: List<Voucher>): Completable
}
