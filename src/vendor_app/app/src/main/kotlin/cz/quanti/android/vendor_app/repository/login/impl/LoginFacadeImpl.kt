package cz.quanti.android.vendor_app.repository.login.impl

import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.utils.*
import io.reactivex.Completable

class LoginFacadeImpl(
    private val loginRepo: LoginRepository,
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
                        getVendor(loggedVendor.id)
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

    private fun getVendor(id: String): Completable {
        return loginRepo.getVendor(id).flatMapCompletable { response ->
            val responseCode = response.first
            val vendor = response.second
            if (isPositiveResponseHttpCode(responseCode)) {
                CurrentVendor.vendor.country = vendor.country
                Completable.complete()
            } else {
                Completable.error(VendorAppException("Cannot get the vendor").apply {
                    this.apiError = true
                    this.apiResponseCode = responseCode
                })
            }
        }
    }
}
