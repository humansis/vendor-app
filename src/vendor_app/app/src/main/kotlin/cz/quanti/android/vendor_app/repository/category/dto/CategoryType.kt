package cz.quanti.android.vendor_app.repository.category.dto

import com.google.gson.annotations.SerializedName

enum class CategoryType {
    ALL,
    @SerializedName("Food")FOOD,
    @SerializedName("Non-food")NONFOOD,
    @SerializedName("Cashback")CASHBACK,
    OTHER
}
