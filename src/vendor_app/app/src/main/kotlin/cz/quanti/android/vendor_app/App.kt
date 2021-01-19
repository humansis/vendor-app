package cz.quanti.android.vendor_app

import androidx.appcompat.app.AppCompatDelegate
import cz.quanti.android.vendor_app.di.KoinInitializer
import cz.quanti.android.vendor_app.repository.AppPreferences
import org.koin.android.ext.android.inject
import quanti.com.kotlinlog.Log
import quanti.com.kotlinlog.android.AndroidLogger
import quanti.com.kotlinlog.base.LogLevel
import quanti.com.kotlinlog.base.LoggerBundle
import quanti.com.kotlinlog.file.FileLogger
import quanti.com.kotlinlog.file.bundle.DayLogBundle
import wtf.qase.appskeleton.core.BaseApp

class App : BaseApp() {

    val preferences: AppPreferences by inject()

    override fun onCreate() {
        super.onCreate()

        // Use custom logger
        Log.initialise(this)
        Log.addLogger(AndroidLogger(LoggerBundle(LogLevel.DEBUG)))
        Log.addLogger(FileLogger(applicationContext, DayLogBundle(maxDaysSaved = 3)))
        Log.useUncheckedErrorHandler()

        KoinInitializer.init(this)

        preferences.init()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
}
