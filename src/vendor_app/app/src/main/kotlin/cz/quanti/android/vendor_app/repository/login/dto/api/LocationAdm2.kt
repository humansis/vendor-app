package cz.quanti.android.vendor_app.repository.login.dto.api

data class LocationAdm2(
    var id: Long = 0,
    var name: String = "",
    var code: String = "",
    var adm1: LocationAdm1? = null
)
