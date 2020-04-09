package cz.quanti.android.vendor_app.repository.login.dto.api

data class LocationAdm4(
    var id: Long = 0,
    var name: String = "",
    var adm3: LocationAdm3? = null,
    var code: String = ""
)
