package cz.quanti.android.vendor_app.repository.category.dto.api

import cz.quanti.android.vendor_app.repository.category.dto.CategoryType

class CategoryApiEntity(
    var id: Long = 0,
    var name: String = "",
    var type: CategoryType,
    var image: String = ""
)
