package cz.quanti.android.vendor_app.utils

import java.util.*

object Constants {
    const val CAMERA_PERMISSION_REQUEST_CODE = 1230
    val SUPPORTED_CURRENCIES: Set<Currency> = Currency.getAvailableCurrencies()
    const val SYNCING_BUTTON_ANIMATION_DURATION_IN_MS: Long = 2500
}
