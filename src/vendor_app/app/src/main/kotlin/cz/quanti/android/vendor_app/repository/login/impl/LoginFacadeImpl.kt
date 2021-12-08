package cz.quanti.android.vendor_app.repository.login.impl

import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.utils.exceptions.LoginException
import cz.quanti.android.vendor_app.repository.utils.exceptions.LoginExceptionState
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.LoginManager
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable

class LoginFacadeImpl(
    private val loginRepo: LoginRepository,
    private val loginManager: LoginManager,
    private val currentVendor: CurrentVendor
) : LoginFacade {

    override fun login(username: String, password: String): Completable {
        loginManager.login(username, password)
        return loginRepo.login(username, password).flatMapCompletable { response ->
            val responseCodeLogin = response.responseCode
            val loggedVendor = response.vendor
            if (isPositiveResponseHttpCode(responseCodeLogin)) {
                loggedVendor.loggedIn = true
                loggedVendor.username = username
                loggedVendor.password = password
                currentVendor.vendor = loggedVendor
                Completable.complete()
            } else {
                Completable.error(
                    LoginException(
                        if (responseCodeLogin == 504) {
                            LoginExceptionState.NO_CONNECTION
                        } else {
                            LoginExceptionState.INVALID_USER_OR_PASSWORD
                        }
                    )
                )
            }
        }
    }

    override fun logout() {
        currentVendor.clear()
    }
}
