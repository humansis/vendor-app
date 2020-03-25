package cz.quanti.android.vendor_app.repository.db.reposiitory

import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.Voucher
import io.reactivex.Completable
import io.reactivex.Single

interface DbRepository {

    fun getProducts(): Single<List<Product>>

    fun deleteProducts(): Completable

    fun saveProduct(product: Product): Completable

    fun saveVoucher(voucher: Voucher): Completable
}
