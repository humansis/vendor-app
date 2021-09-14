package cz.quanti.android.vendor_app.repository.category.dto.api

class CategoryApiEntity(
    var id: Long = 0,
    var name: String = "",
    var type: String = "",
    var image: String? = ""
)

class CategoryPagedApiEntity(
    var totalCount: Long = 0,
    var data: List<CategoryApiEntity> = listOf()
)
