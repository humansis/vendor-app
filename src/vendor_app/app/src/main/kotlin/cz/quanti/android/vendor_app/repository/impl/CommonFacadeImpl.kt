package cz.quanti.android.vendor_app.repository.impl

import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.repository.CommonFacade
import cz.quanti.android.vendor_app.repository.common.DbRepository
import cz.quanti.android.vendor_app.repository.common.VendorServerApiRepository
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.utils.LoginManager
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.hashAndSaltPassword
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class CommonFacadeImpl(
    private val apiRepo: VendorServerApiRepository,
    private val dbRepo: DbRepository,
    private val picasso: Picasso,
    private val loginManager: LoginManager
) :
    CommonFacade {

    override fun login(username: String, password: String): Completable {
        return apiRepo.getSalt(username).flatMapCompletable { saltResponse ->
            if (saltResponse.code() != 200) {
                Completable.error(
                    VendorAppException("Could not obtain salt for the user.")
                        .apply {
                        apiError = true
                        apiResponseCode = saltResponse.code()
                    })
            } else {
                saltResponse.body()?.salt?.let {
                    var saltedPassword =
                        hashAndSaltPassword(
                            it,
                            password
                        )
                    val vendor = Vendor()
                        .apply {
                            this.saltedPassword = saltedPassword
                            this.username = username
                            this.loggedIn = true
                            this.password = saltedPassword
                        }
                    loginManager.user = vendor
                    apiRepo.login(vendor).flatMapCompletable { reloadProductFromServer() }
                }
            }
        }
    }

    override fun reloadProductFromServer(): Completable {
        return apiRepo.getProducts().flatMapCompletable { response ->
            if (response.code() == 200) {
                actualizeDatabase(response.body())
            } else {
                Completable.error(
                    VendorAppException(
                        "Could not get products from server."
                    ).apply {
                    apiError = true
                    apiResponseCode = response.code()
                })
            }
        }
    }

    override fun getProducts(): Single<List<Product>> {
        return dbRepo.getProducts()
    }

    override fun saveVouchers(vouchers: List<Voucher>): Completable {
        return Observable.fromIterable(vouchers).flatMapCompletable { voucher ->
            Completable.fromCallable { dbRepo.saveVoucher(voucher) }
        }
    }

    private fun actualizeDatabase(products: List<Product>?): Completable {
        return if (products == null) {
            throw VendorAppException("Products returned from server were empty.")
        } else {
            dbRepo.deleteProducts().andThen(
                Observable.fromIterable(products).flatMapCompletable { product ->
                    dbRepo.saveProduct(product)
                        .andThen(Completable.fromCallable { picasso.load(product.image) })
                })
        }
    }
}
