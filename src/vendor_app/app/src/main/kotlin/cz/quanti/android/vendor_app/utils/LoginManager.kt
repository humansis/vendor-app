package cz.quanti.android.vendor_app.utils

class LoginManager(private val currentVendor: CurrentVendor) {

    fun login(username: String, saltedPassword: String) {
        currentVendor.vendor.username = username
        currentVendor.vendor.saltedPassword = saltedPassword
    }

    fun getAuthHeader(): String {
        return generateXWSSEHeader(
            currentVendor.vendor.username, currentVendor.vendor.saltedPassword
        )
    }
}
