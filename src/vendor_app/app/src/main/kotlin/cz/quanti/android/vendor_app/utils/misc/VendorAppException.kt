package cz.quanti.android.vendor_app.utils.misc

import java.lang.Exception

class VendorAppException(message: String): Exception(message) {
    var apiError: Boolean = false
    var apiResponseCode: Int = 0
}
