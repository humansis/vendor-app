package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.login.dto.Vendor

object CurrentVendor {
    val emptyVendor = Vendor()

    var preferences: AppPreferences? = null
    var vendor: Vendor
        get() = preferences?.vendor ?: emptyVendor
        set(vendor) {
            preferences?.vendor = vendor
        }

    fun isLoggedIn(): Boolean {
        return vendor.loggedIn
    }

    fun clear() {
        preferences?.vendor = emptyVendor
    }
}
