package cz.quanti.android.vendor_app

import androidx.appcompat.app.AppCompatDelegate
import cz.quanti.android.nfc.logger.NfcLogger
import cz.quanti.android.vendor_app.di.KoinInitializer
import cz.quanti.android.vendor_app.repository.AppPreferences
import org.koin.android.ext.android.inject
import quanti.com.kotlinlog.Log
import quanti.com.kotlinlog.android.AndroidLogger
import quanti.com.kotlinlog.android.MetadataLogger
import quanti.com.kotlinlog.base.LogLevel
import quanti.com.kotlinlog.base.LoggerBundle
import quanti.com.kotlinlog.file.FileLogger
import quanti.com.kotlinlog.file.bundle.CircleLogBundle
import wtf.qase.appskeleton.core.BaseApp

class App : BaseApp() {

    private val preferences: AppPreferences by inject()

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")

        initLogger()

        KoinInitializer.init(this)

        preferences.init()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private fun initLogger() {
        Log.initialise(this)
        Log.addLogger(AndroidLogger(LoggerBundle(LogLevel.DEBUG)))
        val circleLogBundle = CircleLogBundle(LogLevel.VERBOSE, 1, 50, 30, LogLevel.VERBOSE)
        Log.addLogger(FileLogger(applicationContext, circleLogBundle))
        Log.useUncheckedErrorHandler()
        NfcLogger.registerListener(Logger())
        MetadataLogger.customMetadataLambda = {
            val metadata: MutableList<Pair<String, String>> = mutableListOf()
            metadata.add(Pair("VERSION:", getString(R.string.version, BuildConfig.VERSION_NAME)))
            metadata.add(Pair("BUILD_NUMBER:", BuildConfig.BUILD_NUMBER.toString()))
            metadata.add(Pair("USER_ID:", preferences.vendor.id.toString()))
            metadata.add(Pair("VENDOR_ID:", preferences.vendor.vendorId.toString()))
            metadata.add(Pair("VENDOR_USERNAME:", preferences.vendor.username))
            metadata.add(Pair("VENDOR_COUNTRY:", preferences.vendor.country))
            metadata
        }
    }

    private class Logger : NfcLogger.Listener {
        override fun v(tag: String, message: String) {
            Log.v(tag, message)
        }

        override fun e(tag: String, throwable: Throwable) {
            Log.e(tag, throwable)
        }

        override fun d(tag: String, message: String) {
            Log.d(tag, message)
        }

        override fun i(tag: String, message: String) {
            Log.i(tag, message)
        }

        override fun w(tag: String, message: String) {
            Log.w(tag, message)
        }

        override fun e(tag: String, message: String) {
            Log.e(tag, message)
        }
    }

    companion object {
        private val TAG = App::class.java.simpleName
    }
}
