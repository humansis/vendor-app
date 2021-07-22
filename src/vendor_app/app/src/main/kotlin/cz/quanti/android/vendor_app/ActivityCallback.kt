package cz.quanti.android.vendor_app

interface ActivityCallback {
    fun setToolbarVisible (boolean: Boolean)
    fun setBackButtonVisible (boolean: Boolean)
    fun loadNavHeader(currentVendorName: String)
    fun setSubtitle(titleText: String?)
}
