package cz.quanti.android.vendor_app.di


import cz.quanti.android.vendor_app.App
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.main.scanner.viewmodel.ScannerViewModel
import cz.quanti.android.vendor_app.main.vendor.viewmodel.ProductDetailViewModel
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
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
        return module {
            single { AppPreferences(androidContext()) }

            //View model
            viewModel { LoginViewModel() }
            viewModel { VendorViewModel() }
            viewModel { ProductDetailViewModel() }
            viewModel { ScannerViewModel() }
            viewModel { CheckoutViewModel() }
        }
    }
}
