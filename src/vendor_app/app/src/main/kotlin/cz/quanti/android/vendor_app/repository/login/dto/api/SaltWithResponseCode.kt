package cz.quanti.android.vendor_app.repository.login.dto.api

import cz.quanti.android.vendor_app.repository.login.dto.Salt

data class SaltWithResponseCode (
    val salt: Salt,
    val responseCode: Int
)
