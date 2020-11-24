package cz.quanti.android.vendor_app.repository.login.impl

import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.utils.*
import io.reactivex.Completable

class LoginFacadeImpl(
    private val loginRepo: LoginRepository,
    private val loginManager: LoginManager,
    private val currentVendor: CurrentVendor
) : LoginFacade {

    override fun login(username: String, password: String): Completable {
        return loginRepo.getSalt(username).flatMapCompletable { saltResponse ->
            val responseCode = saltResponse.responseCode
            val salt = saltResponse.salt
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
                    val responseCode = response.responseCode
                    val loggedVendor = response.vendor
                    if (isPositiveResponseHttpCode(responseCode)) {
                        loggedVendor.loggedIn = true
                        loggedVendor.username = vendor.username
                        loggedVendor.saltedPassword = vendor.saltedPassword
                        currentVendor.vendor = loggedVendor
                        Completable.complete()
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

    override fun logout() {
        currentVendor.clear()
    }
}
