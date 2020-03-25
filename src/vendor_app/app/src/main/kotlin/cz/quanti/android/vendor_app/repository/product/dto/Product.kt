package cz.quanti.android.vendor_app.repository.product.dto

data class Product(
    var id: Long = 0,
    var name: String = "",
    var image: String = "",
    var unit: String = ""
) {

    fun isEmpty(): Boolean {
        return (id == 0.toLong() && name == "")
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Product) {
            id == other.id && name == other.name && image == other.image && unit == other.unit
        } else {
            false
        }
    }
}
