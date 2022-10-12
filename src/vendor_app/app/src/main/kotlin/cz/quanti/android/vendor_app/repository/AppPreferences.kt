package cz.quanti.android.vendor_app.repository

import android.content.Context
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import org.koin.core.component.KoinComponent
import wtf.qase.appskeleton.core.BasePreferences
import wtf.qase.appskeleton.core.BasePreferencesMigration
import java.util.TreeMap

class AppPreferences(context: Context) :
    BasePreferences(context, VERSION, MIGRATIONS),
    KoinComponent {

    companion object {
        const val VERSION = 2

        val MIGRATIONS = TreeMap<Int, BasePreferencesMigration>()

        private const val USER_ID = "pin_vendor_app_user_id"
        private const val VENDOR_ID = "pin_vendor_app_vendor_id"
        private const val VENDOR_USERNAME = "pin_vendor_app_vendor_username"
        private const val VENDOR_COUNTRY = "pin_vendor_app_vendor_country"
        private const val VENDOR_LOGGED_IN = "pin_vendor_app_vendor_logged_in"
        private const val VENDOR_TOKEN = "pin_vendor_app_vendor_token"

        private const val LAST_SYNCED = "pin_vendor_app_last_synced"

        private const val API_HOST = "pin_vendor_app_api_url"

        private const val CURRENCY = "pin_vendor_app_currency"
    }

    override fun init() {
        MIGRATIONS[2] = BasePreferencesMigration { settings ->
            val userId = settings.getLong(VENDOR_ID, 0)
            settings.edit().putLong(USER_ID, userId).apply()
        }
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
                    this.id = settings.getLong(USER_ID, 0)
                    this.vendorId = settings.getLong(VENDOR_ID, 0)
                    this.username = settings.getString(VENDOR_USERNAME, "").toString()
                    this.country = settings.getString(VENDOR_COUNTRY, "").toString()
                    this.loggedIn = settings.getBoolean(VENDOR_LOGGED_IN, false)
                    this.token = settings.getString(VENDOR_TOKEN, "").toString()
                }
            } catch (e: ClassCastException) {
                settings.edit().remove(VENDOR_ID).apply()
            }
            return vendor
        }
        set(vendor) {
            settings.edit().putLong(USER_ID, vendor.id).apply()
            settings.edit().putLong(VENDOR_ID, vendor.vendorId).apply()
            settings.edit().putString(VENDOR_USERNAME, vendor.username).apply()
            settings.edit().putString(VENDOR_COUNTRY, vendor.country).apply()
            settings.edit().putBoolean(VENDOR_LOGGED_IN, vendor.loggedIn).apply()
            settings.edit().putString(VENDOR_TOKEN, vendor.token).apply()
        }

    var host: String
        get() = settings.getString(API_HOST, "").toString()
        set(url) = settings.edit().putString(API_HOST, url).apply()

    var currency: String
        get() = settings.getString(CURRENCY, "").toString()
        set(currency) = settings.edit().putString(CURRENCY, currency).apply()
}
