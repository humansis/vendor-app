package cz.quanti.android.vendor_app.repository.login.dto.api

data class LocationAdm3(
    var id: Long = 0,
    var name: String = "",
    var adm2: LocationAdm2? = null,
    var code: String = ""
)
