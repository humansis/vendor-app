package cz.quanti.android.vendor_app.repository.category.dto

enum class CategoryType(val backendName: String) {
    ALL("Does not exist on backend"),
    FOOD ("Food"),
    NONFOOD ("Non-Food"),
    CASHBACK ("Cashback"),
    OTHER("Does not exist on backend")
}
