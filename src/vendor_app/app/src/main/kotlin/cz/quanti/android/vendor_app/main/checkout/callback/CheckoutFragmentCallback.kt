package cz.quanti.android.vendor_app.main.checkout.callback

interface CheckoutFragmentCallback {
    fun cancel()
    fun proceed()
    fun scanVoucher()
    fun payByCard()
}
