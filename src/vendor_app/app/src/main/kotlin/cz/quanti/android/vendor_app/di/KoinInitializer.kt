package cz.quanti.android.vendor_app.di

import androidx.room.Room
import cz.quanti.android.vendor_app.App
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.main.scanner.viewmodel.ScannerViewModel
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.login.impl.LoginFacadeImpl
import cz.quanti.android.vendor_app.repository.login.impl.LoginRepositoryImpl
import cz.quanti.android.vendor_app.repository.product.impl.ProductFacadeImpl
import cz.quanti.android.vendor_app.repository.product.impl.ProductRepositoryImpl
import cz.quanti.android.vendor_app.repository.voucher.impl.VoucherFacadeImpl
import cz.quanti.android.vendor_app.repository.voucher.impl.VoucherRepositoryImpl
import cz.quanti.android.vendor_app.utils.ApiManager
import cz.quanti.android.vendor_app.utils.LoginManager
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

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
        val shoppingHolder = ShoppingHolder()
        ApiManager.init(loginManager)

        val db = Room.databaseBuilder(app, VendorDb::class.java, VendorDb.DB_NAME).build()

        // Repository
        val loginRepo = LoginRepositoryImpl()
        val productRepo = ProductRepositoryImpl(db.productDao())
        val voucherRepo = VoucherRepositoryImpl(db.voucherDao(), db.bookletDao())

        // Facade
        val loginFacade =
            LoginFacadeImpl(loginRepo, loginManager)
        val productFacade = ProductFacadeImpl(productRepo)
        val voucherFacade = VoucherFacadeImpl(voucherRepo, productRepo)

        return module {
            single { AppPreferences(androidContext()) }
            single { db }
            single { loginManager }
            single { shoppingHolder }

            // View model
            viewModel { LoginViewModel(shoppingHolder, loginFacade) }
            viewModel { VendorViewModel(shoppingHolder, productFacade, voucherFacade) }
            viewModel { ScannerViewModel(shoppingHolder, voucherFacade) }
            viewModel { CheckoutViewModel(shoppingHolder, voucherFacade) }
        }
    }
}
