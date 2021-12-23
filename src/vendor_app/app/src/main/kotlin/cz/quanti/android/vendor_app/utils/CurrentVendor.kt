package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.login.dto.Vendor

class CurrentVendor(private val preferences: AppPreferences) {
    var vendor: Vendor
        get() = preferences.vendor
        set(vendor) {
            preferences.vendor = vendor
        }

    var url: ApiEnvironments?
        get() {
            return try {
                ApiEnvironments.valueOf(preferences.url)
            } catch (e: Exception) {
                null
            }
        }
        set(url) {
            url?.let {
                preferences.url = url.name
            }
        }

    fun isLoggedIn(): Boolean {
        return vendor.loggedIn && vendor.country != ""
    }

    fun clear() {
        preferences.vendor = Vendor()
    }
}
