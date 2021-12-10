package cz.quanti.android.vendor_app.repository.login.dto.api

data class VendorApiEntity(
    var userId: Long = 0,
    var username: String = "",
    var password: String = "",
    var location: VendorLocationApiEntity = VendorLocationApiEntity(),
    var token: String = ""
)
