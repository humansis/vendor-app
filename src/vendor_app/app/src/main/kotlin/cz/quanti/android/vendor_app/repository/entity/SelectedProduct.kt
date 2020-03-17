package cz.quanti.android.vendor_app.repository.entity

data class SelectedProduct(
    var product: Product = Product(),
    var quantity: Long = 0,
    var price: Long = 0,
    var subTotal: Long = 0,
    var currency: String = ""
) {
}
