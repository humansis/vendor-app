package cz.quanti.android.vendor_app.repository.login.dto.api

data class VendorLocationApiEntity(
    var id: Long = 0,
    var adm1: LocationAdm1? = null,
    var adm2: LocationAdm2? = null,
    var adm3: LocationAdm3? = null,
    var adm4: LocationAdm4? = null
)
