package cz.quanti.android.vendor_app.repository.category.dto

enum class CategoryType(
    val typeId: Int,
    val backendName: String?
) {
    ALL(0, null),
    FOOD (1, "Food"),
    NONFOOD (2, "Non-Food"),
    CASHBACK (3, "Cashback"),
    OTHER(4, null);

    companion object {
        fun getByName(backendName: String): CategoryType {
            return values().find { it.backendName == backendName } ?: OTHER
        }

        fun getById(id: Int): CategoryType {
            return values().find { it.typeId == id } ?: OTHER
        }
    }
}
