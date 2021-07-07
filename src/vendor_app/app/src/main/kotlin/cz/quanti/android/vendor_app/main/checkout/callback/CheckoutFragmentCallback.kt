package cz.quanti.android.vendor_app.main.checkout.callback

import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct

interface CheckoutFragmentCallback {
    fun cancel()
    fun proceed()
    fun scanVoucher()
    fun payByCard()
    fun updateItem(position: Int, item: SelectedProduct, newPrice: Double)
    fun removeItemFromCart(position: Int)
    fun showInvalidPriceEnteredMessage()
}
