package cz.quanti.android.vendor_app.repository.entity

data class Vendor(
    var id: String = "",
    var username: String = "",
    var password: String = "",
    var saltedParrword: String = "",
    var shop: String = "",
    var adress: String = "",
    var loggedIn: Boolean = false,
    var products: Array<String> = arrayOf(),
    var country: String = "",
    var language: String = ""
    ) {
}
