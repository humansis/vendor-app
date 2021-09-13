package cz.quanti.android.vendor_app.repository.product.dto

import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType

data class Product(
    var id: Long = 0,
    var name: String = "",
    var image: String = "",
    var unit: String = "",
    var category: Category = Category(type = CategoryType.OTHER),
    var unitPrice: Double? = 0.0,
    var currency: String? = ""
)
