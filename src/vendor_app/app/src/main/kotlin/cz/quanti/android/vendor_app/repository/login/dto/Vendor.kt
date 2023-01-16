package cz.quanti.android.vendor_app.repository.login.dto

import java.util.Date

data class Vendor(
    var id: Long = 0,
    var vendorId: Long = 0,
    var username: String = "",
    var password: String = "",
    var loggedIn: Boolean = false,
    var token: String = "",
    var refreshToken: String = "",
    var refreshTokenExpiration: Long = 0,
    var country: String = ""
) {

    fun isRefreshTokenExpired(): Boolean {
        return refreshToken.isBlank() || refreshTokenExpiration < Date().time
    }
}
