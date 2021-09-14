package cz.quanti.android.vendor_app.repository.product.dto.api

data class ProductApiEntity(
    var id: Long = 0,
    var name: String = "",
    var image: String = "",
    var unit: String? = "",
    var productCategoryId: Long = 0,
    var unitPrice: Double? = 0.0,
    var currency: String? = ""
)

class ProductPagedApiEntity(
    var totalCount: Long = 0,
    var data: List<ProductApiEntity> = listOf()
)
