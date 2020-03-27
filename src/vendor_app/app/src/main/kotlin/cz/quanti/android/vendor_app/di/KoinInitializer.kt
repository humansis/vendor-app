package cz.quanti.android.vendor_app.di

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.App
import cz.quanti.android.vendor_app.BuildConfig
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.main.scanner.viewmodel.ScannerViewModel
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.common.impl.DbRepositoryImpl
import cz.quanti.android.vendor_app.repository.common.impl.VendorServerApiRepositoryImpl
import cz.quanti.android.vendor_app.repository.impl.CommonFacadeImpl
import cz.quanti.android.vendor_app.utils.LoginManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object KoinInitializer {

    fun init(app: App) {
        val appModule = createAppModule(app)

        startKoin {
            androidLogger()
            androidContext(app)
            modules(listOf(appModule))
        }
    }

    private fun createAppModule(app: App): Module {

        val loginManager = LoginManager()

        val api = Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(createClient(app, loginManager))
            .build().create(VendorAPI::class.java)

        val picasso = Picasso.Builder(app)
            .loggingEnabled(BuildConfig.DEBUG)
            .indicatorsEnabled(BuildConfig.DEBUG)
            .downloader(OkHttp3Downloader(app))
            .build()

        val db = Room.databaseBuilder(app, VendorDb::class.java, VendorDb.DB_NAME).build()

        val apiRepository =
            VendorServerApiRepositoryImpl(
                api
            )
        val dbRepository =
            DbRepositoryImpl(
                db.productDao(),
                db.voucherDao()
            )

        val facade =
            CommonFacadeImpl(
                apiRepository,
                dbRepository,
                picasso,
                loginManager
            )

        return module {
            single { AppPreferences(androidContext()) }
            single { api }
            single { db }
            single { picasso }
            single { loginManager }

            // View model
            viewModel { LoginViewModel(facade) }
            viewModel { VendorViewModel(facade) }
            viewModel { ScannerViewModel() }
            viewModel { CheckoutViewModel(facade) }
        }
    }

    private fun createClient(context: Context, loginManager: LoginManager): OkHttpClient {

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
                // TODO
                loginManager.getAuthHeader()?.let {
                    headersBuilder.add("x-wsse", it)
                }
                headersBuilder.add("country", "KHM")
                val request = oldRequest.newBuilder().headers(headersBuilder.build()).build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }
}
