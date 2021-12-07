package cz.quanti.android.vendor_app.repository

import android.content.Context
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import java.util.TreeMap
import org.koin.core.component.KoinComponent
import wtf.qase.appskeleton.core.BasePreferences
import wtf.qase.appskeleton.core.BasePreferencesMigration

class AppPreferences(context: Context) : BasePreferences(context, VERSION, MIGRATIONS),
    KoinComponent {

    companion object {
        const val VERSION = 1

        val MIGRATIONS = TreeMap<Int, BasePreferencesMigration>()

        private const val VENDOR_ID = "pin_vendor_app_vendor_id"
        private const val VENDOR_USERNAME = "pin_vendor_app_vendor_username"
        private const val VENDOR_SALTED_PASSWORD = "pin_vendor_app_vendor_salted_password"
        private const val VENDOR_COUNTRY = "pin_vendor_app_vendor_country"
        private const val VENDOR_LOGGED_IN = "pin_vendor_app_vendor_logged_in"

        private const val LAST_SYNCED = "pin_vendor_app_last_synced"

        private const val API_URL = "pin_vendor_app_api_url"

        private const val CURRENCY = "pin_vendor_app_currency"
    }

    override fun init() {
    }

    var lastSynced: Long
        get() {
            return settings.getLong(LAST_SYNCED, 0)
        }
        set(lastSynced) {
            settings.edit().putLong(LAST_SYNCED, lastSynced).apply()
        }

    var vendor: Vendor
        get() {
            val vendor = Vendor()
            try {
                vendor.apply {
                    this.id = settings.getLong(VENDOR_ID, 0)
                    this.username = settings.getString(VENDOR_USERNAME, "")!!
                    this.saltedPassword = settings.getString(VENDOR_SALTED_PASSWORD, "")!!
                    this.country = settings.getString(VENDOR_COUNTRY, "")!!
                    this.loggedIn = settings.getBoolean(VENDOR_LOGGED_IN, false)
                }
            } catch (e: ClassCastException) {
                settings.edit().remove(VENDOR_ID).apply()
            }
            return vendor
        }
        set(vendor) {
            settings.edit().putLong(VENDOR_ID, vendor.id).apply()
            settings.edit().putString(VENDOR_USERNAME, vendor.username).apply()
            settings.edit().putString(VENDOR_SALTED_PASSWORD, vendor.saltedPassword).apply()
            settings.edit().putString(VENDOR_COUNTRY, vendor.country).apply()
            settings.edit().putBoolean(VENDOR_LOGGED_IN, vendor.loggedIn).apply()
        }

    var url: String
        get() = settings.getString(API_URL, "").toString()
        set(url) = settings.edit().putString(API_URL, url).apply()

    var currency: String
        get() = settings.getString(CURRENCY, "").toString()
        set(currency) = settings.edit().putString(CURRENCY, currency).apply()
}
