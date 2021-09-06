package cz.quanti.android.vendor_app.repository.category.dto

data class Category(
    var id: Long = 0,
    var name: String = "",
    var type: CategoryType,
    var image: String = ""
)
