package cz.quanti.android.vendor_app.repository.utils.interceptor

import cz.quanti.android.vendor_app.utils.ApiEnvironments
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class HostUrlInterceptor : Interceptor {

    @Volatile
    private var host: ApiEnvironments? = null

    fun setHost(host: ApiEnvironments?) {
        this.host = host
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        host?.let { host ->
            val newUrl = request.url().newBuilder()
                .host(host.getUrl())
                .build()
            request = request.newBuilder()
                .url(newUrl)
                .build()
        }
        return chain.proceed(request)
    }
}
