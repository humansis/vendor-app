package cz.quanti.android.vendor_app.repository.purchase.dto

import cz.quanti.android.vendor_app.repository.product.dto.Product

data class SelectedProduct(
    var product: Product = Product(),
    var price: Double = 0.0,
    var currency: String = ""
) {
    override fun equals(other: Any?): Boolean {
        return if (other is SelectedProduct) {
            product == other.product && price.equals(other.price) && currency == other.currency
        } else {
            false
        }
    }
}
