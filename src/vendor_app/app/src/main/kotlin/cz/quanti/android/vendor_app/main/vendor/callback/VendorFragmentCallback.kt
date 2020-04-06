package cz.quanti.android.vendor_app.main.vendor.callback

import cz.quanti.android.vendor_app.repository.product.dto.Product

interface VendorFragmentCallback {
    fun chooseProduct(product: Product)
    fun getCurrency(): String
    fun setCurrency(currency: String)
    fun showCart()
    fun showProducts()
    fun getSelectedProduct(): Product
}
