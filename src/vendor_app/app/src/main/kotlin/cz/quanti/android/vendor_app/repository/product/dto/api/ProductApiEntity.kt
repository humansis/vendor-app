package cz.quanti.android.vendor_app.repository.product.dto.api

data class ProductApiEntity(
    var id: Long = 0,
    var name: String = "",
    var image: String = "",
    var unit: String? = ""
)
