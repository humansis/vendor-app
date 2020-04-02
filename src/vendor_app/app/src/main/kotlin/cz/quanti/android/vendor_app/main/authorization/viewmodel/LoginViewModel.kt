package cz.quanti.android.vendor_app.main.authorization.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import io.reactivex.Completable

class LoginViewModel(private val loginFacade: LoginFacade) : ViewModel() {

    fun login(username: String, password: String): Completable {
        return loginFacade.login(username, password)
    }

    fun logout() {
        // TODO
    }
}
