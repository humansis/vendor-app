package cz.quanti.android.vendor_app.utils

class LoginManager(private val currentVendor: CurrentVendor) {

    fun login(username: String, password: String) {
        currentVendor.vendor.username = username
        currentVendor.vendor.password = password
    }

    fun getAuthToken(): String {
        return currentVendor.vendor.token
    }
}
