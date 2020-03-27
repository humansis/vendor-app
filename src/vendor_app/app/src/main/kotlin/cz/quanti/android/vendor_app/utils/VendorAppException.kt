package cz.quanti.android.vendor_app.utils

class VendorAppException(message: String) : Exception(message) {
    var apiError: Boolean = false
    var apiResponseCode: Int = 0
}
