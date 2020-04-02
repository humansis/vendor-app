package cz.quanti.android.vendor_app.main.vendor.callback

import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct

interface VendorFragmentCallback {
    fun chooseProduct(product: Product)
    fun getCurrency(): String
    fun removeFromCart(position: Int)
    fun getShoppingCart(): List<SelectedProduct>
    fun clearCart()
    fun setCurrency(currency: String)
    fun addToShoppingCart(product: SelectedProduct)
}
