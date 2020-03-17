package cz.quanti.android.vendor_app.repository

import android.content.Context
import org.koin.core.KoinComponent
import wtf.qase.appskeleton.core.BasePreferences
import wtf.qase.appskeleton.core.BasePreferencesMigration
import java.util.TreeMap

class AppPreferences(context: Context) : BasePreferences(context, VERSION, MIGRATIONS), KoinComponent {

    companion object {
        const val VERSION = 1

        val MIGRATIONS = TreeMap<Int, BasePreferencesMigration>()

    }

    override fun init() {
    }

}
