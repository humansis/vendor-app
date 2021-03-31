package cz.quanti.android.vendor_app

interface ActivityCallback {
    fun showDot(boolean: Boolean)
    fun setToolbarVisible (boolean: Boolean)
    fun loadNavHeader(currentVendorName: String)
}
