package cz.quanti.android.vendor_app.utils

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import cz.quanti.android.vendor_app.BuildConfig
import cz.quanti.android.vendor_app.repository.VendorAPI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiManager {
    private lateinit var api: VendorAPI
    private lateinit var loginManager: LoginManager

    fun getApi() = api

    fun changeUrl(url: String) {
        api = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().serializeNulls().create()
                )
            )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(createClient(loginManager))
            .build().create(VendorAPI::class.java)
    }

    fun init(loginManager: LoginManager) {
        this.loginManager = loginManager
        changeUrl(BuildConfig.API_URL)
    }

    private fun createClient(loginManager: LoginManager): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .callTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .addInterceptor { chain ->
                val oldRequest = chain.request()
                val headersBuilder = oldRequest.headers().newBuilder()
                loginManager.getAuthHeader()?.let {
                    headersBuilder.add("x-wsse", it)
                }
                headersBuilder.add("country", getCountry())
                val request = oldRequest.newBuilder().headers(headersBuilder.build()).build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    private fun getCountry(): String {
        return CurrentVendor.vendor.country
    }
}
