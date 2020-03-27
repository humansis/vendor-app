package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.repository.login.dto.Vendor

class LoginManager {
    var user: Vendor? = null

    fun getAuthHeader(): String? {
        if (user == null) return null

        return generateXWSSEHeader(
            user?.username ?: "", user?.saltedPassword ?: "", true
        )
    }
}
