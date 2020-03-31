package cz.quanti.android.vendor_app.repository.login.impl

import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.product.ProductRepository
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.voucher.VoucherRepository
import cz.quanti.android.vendor_app.utils.*
import io.reactivex.Completable
import io.reactivex.Observable

class LoginFacadeImpl(
    private val loginRepo: LoginRepository,
    private val productRepo: ProductRepository,
    private val voucherRepo: VoucherRepository,
    private val picasso: Picasso,
    private val loginManager: LoginManager
) : LoginFacade {

    override fun login(username: String, password: String): Completable {
        return loginRepo.getSalt(username).flatMapCompletable { saltResponse ->
            val responseCode = saltResponse.first
            val salt = saltResponse.second
            if (!isPositiveResponseHttpCode(responseCode)) {
                Completable.error(
                    VendorAppException("Could not obtain salt for the user.")
                        .apply {
                            apiError = true
                            apiResponseCode = responseCode
                        })
            } else {
                var saltedPassword = hashAndSaltPassword(salt.salt, password)
                val vendor = Vendor()
                    .apply {
                        this.saltedPassword = saltedPassword
                        this.username = username
                        this.loggedIn = true
                        this.password = saltedPassword
                    }
                loginManager.login(username, saltedPassword)
                loginRepo.login(vendor).flatMapCompletable { response ->
                    val responseCode = response.first
                    val loggedVendor = response.second
                    if (isPositiveResponseHttpCode(responseCode)) {
                        loggedVendor.loggedIn = true
                        loggedVendor.username = vendor.username
                        loggedVendor.saltedPassword = vendor.saltedPassword
                        CurrentVendor.vendor = loggedVendor
                        syncWithServer()
                    } else {
                        Completable.error(VendorAppException("Cannot login").apply {
                            this.apiError = true
                            this.apiResponseCode = responseCode
                        })
                    }
                }
            }
        }
    }

    override fun syncWithServer(): Completable {
        return sendDataToServer()
            .andThen(getDataFromServer())
    }

    private fun sendDataToServer(): Completable {
        return sendVouchers()
            .andThen(sendDeactivatedBooklets())
    }

    private fun getDataFromServer(): Completable {
        return reloadProductFromServer()
            .andThen(reloadDeactivatedBookletsFromServer())
            .andThen(reloadProtectedBookletsFromServer())
    }

    private fun sendVouchers(): Completable {
        return voucherRepo.getVouchers().flatMapCompletable { vouchers ->
            voucherRepo.sendVouchersToServer(vouchers).flatMapCompletable { responseCode ->
                if (isPositiveResponseHttpCode(responseCode)) {
                    Completable.complete()
                } else {
                    throw VendorAppException("Could not send vouchers to server").apply {
                        apiError = true
                        apiResponseCode = responseCode
                    }
                }
            }
        }
    }

    private fun sendDeactivatedBooklets(): Completable {
        return voucherRepo.getNewlyDeactivatedBooklets().flatMapCompletable { booklets ->
            voucherRepo.sendDeactivatedBookletsToServer(booklets)
                .flatMapCompletable { responseCode ->
                    if (isPositiveResponseHttpCode(responseCode)) {
                        Completable.complete()
                    } else {
                        throw VendorAppException("Could not send booklets to server").apply {
                            apiError = true
                            apiResponseCode = responseCode
                        }
                    }
                }
        }
    }

    private fun reloadProductFromServer(): Completable {
        return productRepo.getProductsFromServer().flatMapCompletable { response ->
            val responseCode = response.first
            val products = response.second

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

    private fun actualizeDatabase(products: List<Product>): Completable {
        return if (products.isEmpty()) {
            throw VendorAppException("Products returned from server were empty.")
        } else {
            productRepo.deleteProducts().andThen(
                Observable.fromIterable(products).flatMapCompletable { product ->
                    productRepo.saveProduct(product)
                        .andThen(Completable.fromCallable { picasso.load(product.image) })
                })
        }
    }

    private fun reloadDeactivatedBookletsFromServer(): Completable {
        return voucherRepo.getDeactivatedBookletsFromServer().flatMapCompletable { response ->
            val responseCode = response.first
            if (isPositiveResponseHttpCode(responseCode)) {
                val booklets = response.second
                voucherRepo.deleteDeactivated()
                    .andThen(Observable.fromIterable(booklets).flatMapCompletable { booklet ->
                        voucherRepo.saveBooklet(booklet)
                    })
            } else {
                throw VendorAppException("Could not load deactivated booklets").apply {
                    this.apiResponseCode = responseCode
                    this.apiError = true
                }
            }
        }
    }

    private fun reloadProtectedBookletsFromServer(): Completable {
        return voucherRepo.getProtectedBookletsFromServer().flatMapCompletable { response ->
            val responseCode = response.first

            if (isPositiveResponseHttpCode(responseCode)) {
                val booklets = response.second
                voucherRepo.deleteProtected()
                    .andThen(Observable.fromIterable(booklets).flatMapCompletable { booklet ->
                        voucherRepo.saveBooklet(booklet)
                    })
            } else {
                throw VendorAppException("Could not load protected booklets").apply {
                    this.apiResponseCode = responseCode
                    this.apiError = true
                }
            }
        }
    }
}
