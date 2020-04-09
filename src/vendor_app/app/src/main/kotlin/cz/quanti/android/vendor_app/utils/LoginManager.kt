package cz.quanti.android.vendor_app.utils

class LoginManager {

    fun login(username: String, saltedPassword: String) {
        CurrentVendor.vendor.username = username
        CurrentVendor.vendor.saltedPassword = saltedPassword
    }

    fun getAuthHeader(): String? {
        return generateXWSSEHeader(
            CurrentVendor.vendor.username, CurrentVendor.vendor.saltedPassword, true
        )
    }
}
