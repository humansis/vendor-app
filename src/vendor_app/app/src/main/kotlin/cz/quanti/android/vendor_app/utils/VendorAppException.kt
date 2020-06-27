package cz.quanti.android.vendor_app.utils

open class VendorAppException(message: String) : Exception(message) {
    var apiError: Boolean = false
    var apiResponseCode: Int = 0
}
