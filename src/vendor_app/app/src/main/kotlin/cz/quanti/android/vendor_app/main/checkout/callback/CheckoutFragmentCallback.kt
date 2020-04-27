package cz.quanti.android.vendor_app.main.checkout.callback

interface CheckoutFragmentCallback {
    fun showCart()
    fun goToPayment()
    fun cancel()
    fun proceed()
    fun scanVoucher()
    fun payByCard()
}
