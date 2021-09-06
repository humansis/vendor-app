package cz.quanti.android.vendor_app.repository.product.dto

import cz.quanti.android.vendor_app.repository.category.dto.Category

data class Product(
    var id: Long = 0,
    var name: String = "",
    var image: String = "",
    var unit: String = "",
    var category: Category? = null,
    var unitPrice: Long? = 0,
    var currency: String? = ""
)
