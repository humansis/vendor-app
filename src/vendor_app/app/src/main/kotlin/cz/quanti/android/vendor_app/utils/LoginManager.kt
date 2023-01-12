package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.repository.login.dto.Vendor

class LoginManager(private val currentVendor: CurrentVendor) {

    fun login(username: String, password: String) {
        currentVendor.vendor.username = username
        currentVendor.vendor.password = password
    }

    fun getAuthToken(): String {
        return currentVendor.vendor.token
    }

    fun getRefreshToken(): String {
        val vendor = currentVendor.vendor
        return vendor.refreshToken
    }

    fun updateTokens(vendor: Vendor) {
        currentVendor.vendor = currentVendor.vendor.copy(
            token = vendor.token,
            refreshToken = vendor.refreshToken,
            refreshTokenExpiration = vendor.refreshTokenExpiration
        )
    }

    fun invalidateTokens() {
        currentVendor.vendor = currentVendor.vendor.copy(
            token = "",
            refreshToken = "",
            refreshTokenExpiration = ""
        )
    }
}
