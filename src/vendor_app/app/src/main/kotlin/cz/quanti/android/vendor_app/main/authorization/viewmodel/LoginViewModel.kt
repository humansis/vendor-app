package cz.quanti.android.vendor_app.main.authorization.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import io.reactivex.Completable

class LoginViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val loginFacade: LoginFacade
) : ViewModel() {

    fun login(username: String, password: String): Completable {
        return loginFacade.login(username, password)
    }

    fun logout() {
        CurrentVendor.clear()
    }

    fun isFirstTimeLoading(): Boolean {
        return shoppingHolder.justStarted
    }

    fun setIsNotFirstTimeLoading() {
        shoppingHolder.justStarted = false
    }
}
