package cz.quanti.android.vendor_app.repository.category.dto

enum class CategoryType(val backendName: String?) {
    ALL(null),
    FOOD ("Food"),
    NONFOOD ("Non-Food"),
    CASHBACK ("Cashback"),
    OTHER(null);

    companion object {
        fun getByName(backendName: String): CategoryType {
            return values().find { it.backendName == backendName } ?: OTHER
        }
    }
}
