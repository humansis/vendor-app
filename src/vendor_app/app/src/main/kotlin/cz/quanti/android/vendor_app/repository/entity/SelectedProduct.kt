package cz.quanti.android.vendor_app.repository.entity

data class SelectedProduct(
    var product: Product = Product(),
    var quantity: Double = 0.0,
    var price: Double = 0.0,
    var subTotal: Double = 0.0,
    var currency: String = ""
) {
    override fun equals(other: Any?): Boolean {
        return if (other is SelectedProduct) {
            product == other.product && price.equals(other.price) && currency == other.currency
        } else {
            false
        }
    }

    fun add(more: SelectedProduct) {
        quantity += more.quantity
        subTotal = quantity*price
    }
}
