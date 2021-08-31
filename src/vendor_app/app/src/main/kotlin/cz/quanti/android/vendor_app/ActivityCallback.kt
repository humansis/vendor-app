package cz.quanti.android.vendor_app

import com.google.android.material.navigation.NavigationView

interface ActivityCallback {
    fun getNavView(): NavigationView
    fun setToolbarVisible (boolean: Boolean)
    fun setSubtitle(titleText: String?)
    fun setDrawerLocked(boolean: Boolean)
    fun setBackButtonEnabled (boolean: Boolean)
    fun setSyncButtonEnabled (boolean: Boolean)
    fun loadNavHeader(currentVendorName: String)
    fun getBackgroundColor(): Int
}
