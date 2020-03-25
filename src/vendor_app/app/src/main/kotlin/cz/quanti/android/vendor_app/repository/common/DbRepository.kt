package cz.quanti.android.vendor_app.repository.common

import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import io.reactivex.Completable
import io.reactivex.Single

interface DbRepository {

    fun getProducts(): Single<List<Product>>

    fun deleteProducts(): Completable

    fun saveProduct(product: Product): Completable

    fun saveVoucher(voucher: Voucher): Completable
}
