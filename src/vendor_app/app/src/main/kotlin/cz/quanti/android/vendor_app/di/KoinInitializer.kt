package cz.quanti.android.vendor_app.di

import androidx.room.Room
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import cz.quanti.android.nfc.PINFacade
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc_io_libray.types.NfcUtil
import cz.quanti.android.vendor_app.App
import cz.quanti.android.vendor_app.BuildConfig
import cz.quanti.android.vendor_app.MainViewModel
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.main.invoices.viewmodel.InvoicesViewModel
import cz.quanti.android.vendor_app.main.scanner.viewmodel.ScannerViewModel
import cz.quanti.android.vendor_app.main.shop.viewmodel.ShopViewModel
import cz.quanti.android.vendor_app.main.transactions.viewmodel.TransactionsViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.booklet.BookletFacade
import cz.quanti.android.vendor_app.repository.booklet.impl.BookletFacadeImpl
import cz.quanti.android.vendor_app.repository.booklet.impl.BookletRepositoryImpl
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.card.impl.CardFacadeImpl
import cz.quanti.android.vendor_app.repository.card.impl.CardRepositoryImpl
import cz.quanti.android.vendor_app.repository.category.CategoryFacade
import cz.quanti.android.vendor_app.repository.category.impl.CategoryFacadeImpl
import cz.quanti.android.vendor_app.repository.category.impl.CategoryRepositoryImpl
import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.deposit.impl.DepositFacadeImpl
import cz.quanti.android.vendor_app.repository.deposit.impl.DepositRepositoryImpl
import cz.quanti.android.vendor_app.repository.invoice.InvoiceFacade
import cz.quanti.android.vendor_app.repository.invoice.impl.InvoiceFacadeImpl
import cz.quanti.android.vendor_app.repository.invoice.impl.InvoiceRepositoryImpl
import cz.quanti.android.vendor_app.repository.log.LogFacade
import cz.quanti.android.vendor_app.repository.log.impl.LogFacadeImpl
import cz.quanti.android.vendor_app.repository.log.impl.LogRepositoryImpl
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.login.impl.LoginFacadeImpl
import cz.quanti.android.vendor_app.repository.login.impl.LoginRepositoryImpl
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.product.impl.ProductFacadeImpl
import cz.quanti.android.vendor_app.repository.product.impl.ProductRepositoryImpl
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.impl.PurchaseFacadeImpl
import cz.quanti.android.vendor_app.repository.purchase.impl.PurchaseRepositoryImpl
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.repository.synchronization.impl.SynchronizationFacadeImpl
import cz.quanti.android.vendor_app.repository.transaction.TransactionFacade
import cz.quanti.android.vendor_app.repository.transaction.impl.TransactionFacadeImpl
import cz.quanti.android.vendor_app.repository.transaction.impl.TransactionRepositoryImpl
import cz.quanti.android.vendor_app.repository.utils.interceptor.HostUrlInterceptor
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationManagerImpl
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.LoginManager
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import quanti.com.kotlinlog.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

        val builder: Retrofit.Builder = Retrofit.Builder()
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().serializeNulls().create()
                )
            )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(createClient(loginManager, hostUrlInterceptor, currentVendor))

        if (BuildConfig.DEBUG) {
            builder.baseUrl("https://" + BuildConfig.STAGE_API_URL + "/api/wsse/vendor-app/")
            // uncomment to use apiary https://app.apiary.io/pinvendor
            // builder.baseUrl("https://private-b3387-pinvendor.apiary-mock.com")
        } else {
            builder.baseUrl("https://" + BuildConfig.RELEASE_API_URL + "/api/wsse/vendor-app/")
        }

        val api = builder.build().create(VendorAPI::class.java)

        val db = Room.databaseBuilder(app, VendorDb::class.java, VendorDb.DB_NAME)
            .addMigrations(
                VendorDb.MIGRATION_2_3,
                VendorDb.MIGRATION_3_4,
                VendorDb.MIGRATION_4_5,
                VendorDb.MIGRATION_5_6,
                VendorDb.MIGRATION_6_7,
                VendorDb.MIGRATION_7_8,
                VendorDb.MIGRATION_8_9,
                VendorDb.MIGRATION_9_10
            )
            .build()

        // Repository
        val loginRepo = LoginRepositoryImpl(api)
        val categoryRepo = CategoryRepositoryImpl(db.categoryDao(), api)
        val productRepo = ProductRepositoryImpl(categoryRepo, db.productDao(), api)
        val bookletRepo = BookletRepositoryImpl(db.bookletDao(), api)
        val cardRepo = CardRepositoryImpl(db.blockedCardDao(), api)
        val purchaseRepo = PurchaseRepositoryImpl(
            db.purchaseDao(),
            db.cardPurchaseDao(),
            db.voucherPurchaseDao(),
            categoryRepo,
            db.productDao(),
            db.purchasedProductDao(),
            db.selectedProductDao(),
            api
        )
        val depositRepo = DepositRepositoryImpl(
            db.reliefPackageDao(),
            api
        )
        val transactionRepo = TransactionRepositoryImpl(
            db.transactionDao(),
            db.transactionPurchaseDao(),
            api
        )
        val invoiceRepo = InvoiceRepositoryImpl(
            db.invoiceDao(),
            api
        )
        val logRepo = LogRepositoryImpl(
            api
        )

        // Facade
        val loginFacade: LoginFacade = LoginFacadeImpl(loginRepo, loginManager, currentVendor)
        val categoryFacade: CategoryFacade = CategoryFacadeImpl(categoryRepo)
        val productFacade: ProductFacade = ProductFacadeImpl(productRepo, app.applicationContext)
        val bookletFacade: BookletFacade = BookletFacadeImpl(bookletRepo)
        val cardFacade: CardFacade = CardFacadeImpl(cardRepo)
        val purchaseFacade: PurchaseFacade = PurchaseFacadeImpl(purchaseRepo, cardRepo)
        val depositFacade: DepositFacade = DepositFacadeImpl(depositRepo)
        val transactionFacade: TransactionFacade = TransactionFacadeImpl(transactionRepo)
        val invoiceFacade: InvoiceFacade = InvoiceFacadeImpl(invoiceRepo)
        val logFacade: LogFacade = LogFacadeImpl(logRepo, app.applicationContext)
        val syncFacade: SynchronizationFacade =
            SynchronizationFacadeImpl(
                bookletFacade,
                cardFacade,
                categoryFacade,
                depositFacade,
                productFacade,
                purchaseFacade,
                transactionFacade,
                invoiceFacade,
                logFacade
            )
        val synchronizationManager: SynchronizationManager =
            SynchronizationManagerImpl(preferences, syncFacade)
        val nfcFacade: VendorFacade = PINFacade(
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
            single { bookletFacade }
            single { loginFacade }
            single { productFacade }
            single { cardFacade }
            single { purchaseFacade }
            single { syncFacade }
            single { nfcFacade }
            single { nfcTagPublisher }
            single { synchronizationManager }

            // View model
            viewModel {
                MainViewModel(
                    nfcFacade,
                    depositFacade,
                    currentVendor,
                    nfcTagPublisher
                )
            }
            viewModel {
                LoginViewModel(
                    loginFacade,
                    hostUrlInterceptor,
                    currentVendor,
                    synchronizationManager
                )
            }
            viewModel {
                ShopViewModel(
                    shoppingHolder,
                    productFacade,
                    currentVendor,
                    synchronizationManager,
                    preferences
                )
            }
            viewModel { ScannerViewModel(shoppingHolder, bookletFacade) }
            viewModel {
                CheckoutViewModel(
                    shoppingHolder,
                    purchaseFacade,
                    nfcFacade,
                    cardFacade,
                    depositFacade,
                    currentVendor,
                    nfcTagPublisher
                )
            }
            viewModel { InvoicesViewModel(invoiceFacade, synchronizationManager) }
            viewModel {
                TransactionsViewModel(
                    transactionFacade,
                    synchronizationManager,
                    syncFacade
                )
            }
        }
    }

    private fun createClient(
        loginManager: LoginManager,
        hostUrlInterceptor: HostUrlInterceptor,
        currentVendor: CurrentVendor
    ): OkHttpClient {

        val logging = HttpLoggingInterceptor { message -> Log.d("OkHttp", message) }

        logging.level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .callTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .addInterceptor(hostUrlInterceptor)
            .addInterceptor { chain ->
                val oldRequest = chain.request()
                val headersBuilder = oldRequest.headers().newBuilder()
                loginManager.getAuthHeader().let {
                    headersBuilder.add("X-Wsse", it)
                }
                headersBuilder.add("Country", getCountry(currentVendor))
                headersBuilder.add("Version-Name", BuildConfig.VERSION_NAME)
                headersBuilder.add("Build-Number", BuildConfig.BUILD_NUMBER.toString())
                headersBuilder.add("Build-Type", BuildConfig.BUILD_TYPE)
                val request = oldRequest.newBuilder().headers(headersBuilder.build()).build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    private fun getCountry(currentVendor: CurrentVendor): String {
        return currentVendor.vendor.country
    }
}
