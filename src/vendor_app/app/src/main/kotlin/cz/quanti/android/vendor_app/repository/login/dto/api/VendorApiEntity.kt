package cz.quanti.android.vendor_app.repository.login.dto.api

data class VendorApiEntity(
    var id: Long = 0,
    var username: String = "",
    var password: String = "",
    var token: String = "",
    var countryISO3: String = ""
)
