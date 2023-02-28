package cz.quanti.android.vendor_app.repository

import cz.quanti.android.vendor_app.repository.login.dto.api.RefreshTokenRequest
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorApiEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshTokenAPI {

    @POST("v2/login/token/refresh")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): VendorApiEntity
}
