package cz.quanti.android.vendor_app

import com.google.android.material.navigation.NavigationView

interface ActivityCallback {
    fun getNavView(): NavigationView
    fun setToolbarVisible(visible: Boolean)
    fun setSubtitle(titleText: String?)
    fun setDrawerLocked(locked: Boolean)
    fun setBackButtonEnabled(enabled: Boolean)
    fun setSyncButtonEnabled(enabled: Boolean)
    fun setUpBackground()
    fun loadNavHeader(currentVendorName: String)
}
