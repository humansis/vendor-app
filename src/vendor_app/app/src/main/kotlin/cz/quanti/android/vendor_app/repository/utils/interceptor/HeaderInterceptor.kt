package cz.quanti.android.vendor_app.repository.utils.interceptor

import cz.quanti.android.vendor_app.BuildConfig
import cz.quanti.android.vendor_app.repository.RefreshTokenAPI
import cz.quanti.android.vendor_app.repository.login.dto.api.RefreshTokenRequest
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.LoginManager
import cz.quanti.android.vendor_app.utils.getPayload
import cz.quanti.android.vendor_app.utils.toVendor
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import quanti.com.kotlinlog.Log

class HeaderInterceptor(
    private val loginManager: LoginManager,
    private val refreshTokenApi: RefreshTokenAPI,
    private val currentVendor: CurrentVendor
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val headersBuilder = oldRequest.headers().newBuilder()
        runBlocking {
            headersBuilder.handleAuthorizationHeader(oldRequest)
            headersBuilder.add("Country", getCountry(currentVendor))
            headersBuilder.add("Version-Name", BuildConfig.VERSION_NAME)
            headersBuilder.add("Build-Number", BuildConfig.BUILD_NUMBER.toString())
            headersBuilder.add("Build-Type", BuildConfig.BUILD_TYPE)
        }
        val request = oldRequest.newBuilder().headers(headersBuilder.build()).build()
        return chain.proceed(request)
    }

    private suspend fun Headers.Builder.handleAuthorizationHeader(oldRequest: Request) {
        val authToken = loginManager.getAuthToken()
        if (!authToken.isBlankOrExpired()) {
            this.add("Authorization", "Bearer $authToken")
        } else {
            Log.d(
                TAG,
                "Auth token is blank or expiring soon, acquiring new token for request ${oldRequest.method()} ${oldRequest.url()}"
            )
            val refreshToken = loginManager.getRefreshToken()
            if (refreshToken.isNotBlank()) {
                try {
                    val response = refreshTokenApi.refreshToken(RefreshTokenRequest(refreshToken))
                    loginManager.updateTokens(response.toVendor())
                    this.add("Authorization", "Bearer ${response.token}")
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        e,
                        "Refresh token request ended with an exception. Not using Authorization token header and deleting saved tokens so app logs out before next sync"
                    )
                    loginManager.invalidateTokens()
                }
            } else {
                Log.e(
                    TAG,
                    "Refresh token not available. Not using Authorization token header and deleting saved tokens so app logs out before next sync"
                )
                loginManager.invalidateTokens()
            }
        }
    }

    private fun String.isBlankOrExpired(): Boolean {
        return if (this.isNotBlank()) {
            getPayload(this).isExpired()
        } else {
            false
        }
    }

    private fun getCountry(currentVendor: CurrentVendor): String {
        return currentVendor.vendor.country
    }

    companion object {
        const val TAG = "HeaderInterceptor"
    }
}
