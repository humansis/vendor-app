package cz.quanti.android.vendor_app.repository.login.impl

import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.product.ProductRepository
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.LoginManager
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.hashAndSaltPassword
import io.reactivex.Completable
import io.reactivex.Observable

class LoginFacadeImpl(
    private val loginRepo: LoginRepository,
    private val productRepo: ProductRepository,
    private val picasso: Picasso,
    private val loginManager: LoginManager
) : LoginFacade {

    override fun login(username: String, password: String): Completable {
        return loginRepo.getSalt(username).flatMapCompletable { saltResponse ->
            val responseCode = saltResponse.first
            val salt = saltResponse.second
            if (responseCode != 200) {
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
                    if (responseCode == 200) {
                        loggedVendor.loggedIn = true
                        loggedVendor.username = vendor.username
                        loggedVendor.saltedPassword = vendor.saltedPassword
                        CurrentVendor.vendor = loggedVendor
                        reloadProductFromServer()
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

    private fun reloadProductFromServer(): Completable {
        return productRepo.getProductsFromServer().flatMapCompletable { response ->
            val responseCode = response.first
            val products = response.second

            if (responseCode == 200) {
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
}
