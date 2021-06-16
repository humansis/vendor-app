package cz.quanti.android.vendor_app.repository.product.dto

import android.graphics.drawable.Drawable

data class Product(
    var id: Long = 0,
    var name: String = "",
    var image: String = "",
    var drawable: Drawable? = null,
    var unit: String = ""
) {

    fun isEmpty(): Boolean {
        return (id == 0.toLong() && name == "")
    }

}
