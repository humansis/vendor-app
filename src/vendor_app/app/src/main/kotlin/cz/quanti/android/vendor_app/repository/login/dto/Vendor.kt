package cz.quanti.android.vendor_app.repository.login.dto

import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.getPayload
import java.util.Date

data class Vendor(
    var id: Long = 0,
    var vendorId: Long = 0,
    var username: String = "",
    var password: String = "",
    var loggedIn: Boolean = false,
    var country: String = "",
    var token: String = ""
) {
    fun isTokenExpired(numberOfPurchases: Long): Boolean {
        return getPayload(token).let { payload ->
            val tokenExpirationInMillis = payload.exp * 1000
            val numberOfRequests = SynchronizationSubject.values().size
            val timeoutInMillis = 330000
            val timeReserveInMillis = (numberOfPurchases + numberOfRequests) * timeoutInMillis
            (tokenExpirationInMillis - timeReserveInMillis) < Date().time
        }
    }
}
