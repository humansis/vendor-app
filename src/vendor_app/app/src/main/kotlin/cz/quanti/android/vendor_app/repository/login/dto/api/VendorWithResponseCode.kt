package cz.quanti.android.vendor_app.repository.login.dto.api

import cz.quanti.android.vendor_app.repository.login.dto.Vendor

data class VendorWithResponseCode(
    val vendor: Vendor,
    val responseCode: Int
)
