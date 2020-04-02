package cz.quanti.android.vendor_app.main.vendor.callback

interface ShoppingCartFragmentCallback {
    fun getCurrency(): String
    fun removeItemFromCart(position: Int)
}
