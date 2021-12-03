package cz.quanti.android.vendor_app.repository.login.dto

data class Vendor(
    var id: Long = 0,
    var username: String = "",
    var password: String = "",
    var saltedPassword: String = "",
    var loggedIn: Boolean = false,
    var country: String = ""
)
