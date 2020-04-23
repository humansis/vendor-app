package cz.quanti.android.vendor_app.main.authorization.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.utils.interceptor.HostUrlInterceptor
import cz.quanti.android.vendor_app.utils.ApiEnvironments
import cz.quanti.android.vendor_app.utils.CurrentVendor
import io.reactivex.Completable

class LoginViewModel(
    private val loginFacade: LoginFacade,
    private val hostUrlInterceptor: HostUrlInterceptor
) : ViewModel() {

    fun login(username: String, password: String): Completable {
        return loginFacade.login(username, password)
    }

    fun logout() {
        CurrentVendor.clear()
    }

    fun setApiHost(host: ApiEnvironments) {
        hostUrlInterceptor.setHost(host)
    }
}
