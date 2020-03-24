package cz.quanti.android.vendor_app.main.authorization.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.facade.CommonFacade
import cz.quanti.android.vendor_app.utils.misc.VendorAppException
import io.reactivex.Completable

class LoginViewModel(private val facade: CommonFacade) : ViewModel() {

    fun login(username: String, password: String): Completable {
        return facade.login(username, password).flatMapCompletable { response ->
            if (response.code() == 200) {
                facade.reloadProductFromServer()
            } else {
                throw VendorAppException("Login failed.").apply {
                    apiError = true
                    apiResponseCode = response.code()
                }
            }
        }
    }

    fun logoff() {
        // TODO
    }
}
