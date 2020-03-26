package cz.quanti.android.vendor_app.repository

import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import io.reactivex.Completable
import io.reactivex.Single

interface CommonFacade {
    fun login(username: String, password: String): Completable

    fun reloadProductFromServer(): Completable

    fun getProducts(): Single<List<Product>>

    fun saveVouchers(vouchers: List<Voucher>): Completable
}
