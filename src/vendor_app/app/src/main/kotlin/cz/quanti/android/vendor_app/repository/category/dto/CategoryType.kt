package cz.quanti.android.vendor_app.repository.category.dto

import cz.quanti.android.vendor_app.R

enum class CategoryType(
    val typeId: Int,
    val backendName: String?,
    val stringRes: Int?
) {
    ALL(0, null, null),
    FOOD(1, "Food", R.string.food),
    NONFOOD(2, "Non-Food", R.string.nonfood),
    CASHBACK(3, "Cashback", R.string.cashback),
    OTHER(4, null, null);

    companion object {
        fun getByName(backendName: String): CategoryType {
            return values().find { it.backendName == backendName } ?: OTHER
        }

        fun getById(id: Int): CategoryType {
            return values().find { it.typeId == id } ?: OTHER
        }
    }
}
