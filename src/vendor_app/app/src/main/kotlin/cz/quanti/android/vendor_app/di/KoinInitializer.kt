package cz.quanti.android.vendor_app.di

import androidx.room.Room
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import cz.quanti.android.nfc.PINFacade
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc_io_libray.types.NfcUtil
import cz.quanti.android.vendor_app.App
import cz.quanti.android.vendor_app.BuildConfig
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.main.scanner.viewmodel.ScannerViewModel
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.card.impl.CardFacadeImpl
import cz.quanti.android.vendor_app.repository.card.impl.CardRepositoryImpl
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.login.impl.LoginFacadeImpl
import cz.quanti.android.vendor_app.repository.login.impl.LoginRepositoryImpl
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.impl.ProductFacadeImpl
import cz.quanti.android.vendor_app.repository.product.impl.ProductRepositoryImpl
import cz.quanti.android.vendor_app.repository.utils.interceptor.HostUrlInterceptor
import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.repository.voucher.impl.VoucherFacadeImpl
import cz.quanti.android.vendor_app.repository.voucher.impl.VoucherRepositoryImpl
import cz.quanti.android.vendor_app.utils.*
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

        val preferences = AppPreferences(app)
        val shoppingHolder = ShoppingHolder()
        val hostUrlInterceptor = HostUrlInterceptor()
        val currentVendor = CurrentVendor(preferences)
        val loginManager = LoginManager(currentVendor)

        val api = Retrofit.Builder()
            .baseUrl("https://" + BuildConfig.API_URL + "/api/wsse/vendor-app/v1/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().serializeNulls().create()
                )
            )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(createClient(loginManager, hostUrlInterceptor, currentVendor))
            .build().create(VendorAPI::class.java)


        val db = Room.databaseBuilder(app, VendorDb::class.java, VendorDb.DB_NAME)
            .addMigrations(getDbMigration1to2())
            .build()

        // Repository
        val loginRepo = LoginRepositoryImpl(api)
        val productRepo = ProductRepositoryImpl(db.productDao(), api)
        val voucherRepo = VoucherRepositoryImpl(
            db.voucherDao(),
            db.bookletDao(),
            db.voucherPurchaseDao(),
            db.selectedProductDao(),
            api
        )
        val cardRepo = CardRepositoryImpl(db.cardPaymentDao(), db.blockedCardDao(), api)

        // Facade
        val loginFacade: LoginFacade =
            LoginFacadeImpl(loginRepo, loginManager, currentVendor)
        val productFacade: ProductFacade = ProductFacadeImpl(productRepo)
        val voucherFacade: VoucherFacade = VoucherFacadeImpl(voucherRepo, productRepo)
        val cardFacade: CardFacade = CardFacadeImpl(cardRepo)

        val nfcFacade: VendorFacade = PINFacade(
            BuildConfig.APP_VESION,
            NfcUtil.hexStringToByteArray(BuildConfig.MASTER_KEY),
            NfcUtil.hexStringToByteArray(BuildConfig.APP_ID)
        )

        val nfcTagPublisher = NfcTagPublisher()

        return module {
            single { preferences }
            single { db }
            single { api }
            single { hostUrlInterceptor }
            single { loginManager }
            single { shoppingHolder }
            single { currentVendor }
            single { voucherFacade }
            single { loginFacade }
            single { productFacade }
            single { cardFacade }
            single { nfcFacade }
            single { nfcTagPublisher }

            // View model
            viewModel { LoginViewModel(loginFacade, hostUrlInterceptor, currentVendor) }
            viewModel {
                VendorViewModel(
                    shoppingHolder,
                    productFacade,
                    voucherFacade,
                    cardFacade,
                    preferences
                )
            }
            viewModel { ScannerViewModel(shoppingHolder, voucherFacade) }
            viewModel {
                CheckoutViewModel(
                    shoppingHolder,
                    voucherFacade,
                    cardFacade,
                    nfcFacade,
                    currentVendor,
                    nfcTagPublisher
                )
            }
        }
    }

    private fun createClient(
        loginManager: LoginManager,
        hostUrlInterceptor: HostUrlInterceptor,
        currentVendor: CurrentVendor
    ): OkHttpClient {

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
                headersBuilder.add("country", getCountry(currentVendor))
                val request = oldRequest.newBuilder().headers(headersBuilder.build()).build()
                chain.proceed(request)
            }
            .addInterceptor(hostUrlInterceptor)
            .addInterceptor(logging)
            .build()
    }

    private fun getCountry(currentVendor: CurrentVendor): String {
        return currentVendor.vendor.country
    }
}
