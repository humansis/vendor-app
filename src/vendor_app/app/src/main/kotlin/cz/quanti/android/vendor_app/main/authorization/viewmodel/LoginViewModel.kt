package cz.quanti.android.vendor_app.main.authorization.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.utils.interceptor.HostUrlInterceptor
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.utils.ApiEnvironments
import cz.quanti.android.vendor_app.utils.CurrentVendor
import io.reactivex.Completable

class LoginViewModel(
    private val loginFacade: LoginFacade,
    private val hostUrlInterceptor: HostUrlInterceptor,
    private val currentVendor: CurrentVendor,
    private val synchronizationManager: SynchronizationManager
) : ViewModel() {

    private val isNetworkConnectedLD = MutableLiveData<Boolean>()

    fun login(username: String, password: String): Completable {
        return loginFacade.login(username, password)
    }

    fun setApiHost(host: ApiEnvironments) {
        hostUrlInterceptor.setHost(host)
        currentVendor.url = host
    }

    fun getApiHost(): ApiEnvironments? {
        return currentVendor.url
    }

    fun isVendorLoggedIn(): Boolean {
        return currentVendor.isLoggedIn()
    }

    fun getCurrentVendorName(): String {
        return currentVendor.vendor.username
    }

    fun onLogin(activityCallback: ActivityCallback) {
        activityCallback.loadNavHeader(currentVendor.vendor.username)
        synchronizationManager.synchronizeWithServer()
    }

    fun isNetworkConnected(available: Boolean) {
        isNetworkConnectedLD.value = available
    }

    fun isNetworkConnected(): LiveData<Boolean> {
        return isNetworkConnectedLD
    }
}
