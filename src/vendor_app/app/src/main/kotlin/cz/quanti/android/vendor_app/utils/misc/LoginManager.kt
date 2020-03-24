package cz.quanti.android.vendor_app.utils.misc

import cz.quanti.android.vendor_app.repository.entity.Vendor

object LoginManager {
    var user: Vendor? = null

    fun getAuthHeader(): String? {
        if (user == null) return null

        return generateXWSSEHeader(user?.username ?: "", user?.saltedPassword ?: "", true)
    }
}
