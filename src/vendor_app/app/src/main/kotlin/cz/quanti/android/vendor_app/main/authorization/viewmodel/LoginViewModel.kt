package cz.quanti.android.vendor_app.main.authorization.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.CommonFacade
import io.reactivex.Completable

class LoginViewModel(private val facade: CommonFacade) : ViewModel() {

    fun login(username: String, password: String): Completable {
        return facade.login(username, password)
    }

    fun logout() {
        // TODO
    }
}
