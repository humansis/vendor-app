package cz.quanti.android.vendor_app

import com.google.android.material.navigation.NavigationView

interface ActivityCallback {
    fun getNavView(): NavigationView
    fun setToolbarVisible (boolean: Boolean)
    fun setBackButtonVisible (boolean: Boolean)
    fun loadNavHeader(currentVendorName: String)
    fun setSubtitle(titleText: String?)
}
