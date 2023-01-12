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
        const val VERSION = 3

        val MIGRATIONS = TreeMap<Int, BasePreferencesMigration>()

        private const val USER_ID = "pin_vendor_app_user_id"
        private const val VENDOR_ID = "pin_vendor_app_vendor_id"
        private const val VENDOR_USERNAME = "pin_vendor_app_vendor_username"
        private const val VENDOR_COUNTRY = "pin_vendor_app_vendor_country"
        private const val VENDOR_LOGGED_IN = "pin_vendor_app_vendor_logged_in"
        private const val VENDOR_TOKEN = "pin_vendor_app_vendor_token"
        private const val VENDOR_REFRESH_TOKEN = "pin_vendor_app_vendor_refresh_token"
        private const val VENDOR_REFRESH_TOKEN_EXPIRATION = "pin_vendor_app_vendor_refresh_token_expiration"

        private const val LAST_RD_SYNC = "pin_vendor_app_last_relief_package_sync"

        private const val API_HOST = "pin_vendor_app_api_url"

        private const val CURRENCY = "pin_vendor_app_currency"
    }

    override fun init() {
        MIGRATIONS[2] = BasePreferencesMigration { settings ->
            val userId = settings.getLong(VENDOR_ID, 0)
            settings.edit().putLong(USER_ID, userId).apply()
        }
        MIGRATIONS[3] = BasePreferencesMigration { settings ->
            val lastSyncedKey = "pin_vendor_app_last_synced"
            settings.edit().remove(lastSyncedKey).apply()
        }
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
                    this.refreshToken = settings.getString(VENDOR_REFRESH_TOKEN, "").toString()
                    this.refreshTokenExpiration = settings.getString(VENDOR_REFRESH_TOKEN_EXPIRATION, "").toString()
                }
            } catch (e: ClassCastException) {
                settings.edit().remove(VENDOR_ID).apply()
            }
            return vendor
        }
        set(vendor) {
            if (vendor.vendorId != settings.getLong(VENDOR_ID, 0)) {
                lastReliefPackageSync = null
            }
            settings.edit().putLong(USER_ID, vendor.id).apply()
            settings.edit().putLong(VENDOR_ID, vendor.vendorId).apply()
            settings.edit().putString(VENDOR_USERNAME, vendor.username).apply()
            settings.edit().putString(VENDOR_COUNTRY, vendor.country).apply()
            settings.edit().putBoolean(VENDOR_LOGGED_IN, vendor.loggedIn).apply()
            settings.edit().putString(VENDOR_TOKEN, vendor.token).apply()
            settings.edit().putString(VENDOR_REFRESH_TOKEN, vendor.refreshToken).apply()
            settings.edit().putString(VENDOR_REFRESH_TOKEN_EXPIRATION, vendor.refreshToken).apply()
        }

    var host: String
        get() = settings.getString(API_HOST, "").toString()
        set(url) = settings.edit().putString(API_HOST, url).apply()

    var currency: String
        get() = settings.getString(CURRENCY, "").toString()
        set(currency) = settings.edit().putString(CURRENCY, currency).apply()

    var lastReliefPackageSync: String?
        get() {
            return settings.getString(LAST_RD_SYNC, "")?.let { lastSync ->
                lastSync.ifBlank { null }
            }
        }
        set(lastReliefPackageSync) {
            settings.edit().putString(LAST_RD_SYNC, lastReliefPackageSync).apply()
        }
}
