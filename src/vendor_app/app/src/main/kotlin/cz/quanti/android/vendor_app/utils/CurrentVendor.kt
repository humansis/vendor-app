package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.login.dto.Vendor

class CurrentVendor(private val preferences: AppPreferences) {
    var vendor: Vendor
        get() = preferences.vendor
        set(vendor) {
            preferences.vendor = vendor
        }

    var host: ApiEnvironment?
        get() = ApiEnvironment.find(preferences.host)
        set(host) {
            host?.let {
                preferences.host = host.title
            }
        }

    fun isLoggedIn(): Boolean {
        return vendor.loggedIn && vendor.country != ""
    }

    fun clear() {
        preferences.lastReliefPackageSync = null
        preferences.vendor = Vendor()
    }
}
