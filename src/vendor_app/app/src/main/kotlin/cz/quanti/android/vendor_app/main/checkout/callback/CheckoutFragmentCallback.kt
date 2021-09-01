package cz.quanti.android.vendor_app.main.checkout.callback

import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct

interface CheckoutFragmentCallback {
    fun cancel()
    fun proceed()
    fun scanVoucher()
    fun updateItem(item: SelectedProduct, newPrice: Double)
    fun removeItemFromCart(product: SelectedProduct)
    fun showInvalidPriceEnteredMessage()
}
