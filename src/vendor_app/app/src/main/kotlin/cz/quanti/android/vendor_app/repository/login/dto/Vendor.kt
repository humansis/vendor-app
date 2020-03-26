package cz.quanti.android.vendor_app.repository.login.dto

import com.google.gson.annotations.SerializedName

data class Vendor(
    var id: String = "",
    var username: String = "",
    var password: String = "",
    @SerializedName("salted_password")
    var saltedPassword: String = "",
    var shop: String = "",
    var adress: String = "",
    var loggedIn: Boolean = false,
    var products: Array<String> = arrayOf(),
    var country: String = "",
    var language: String = ""
)
