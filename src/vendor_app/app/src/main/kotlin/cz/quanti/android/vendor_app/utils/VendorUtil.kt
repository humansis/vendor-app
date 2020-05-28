package cz.quanti.android.vendor_app.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.text.DecimalFormat
import kotlin.math.roundToInt

fun getStringFromDouble(double: Double): String {
    return when {
        double % 1.0 < 0.001 -> {
            double.roundToInt().toString()
        }
        (double * 10) % 1.0 < 0.01 -> {
            DecimalFormat("#.#").format(double)
        }
        else -> {
            DecimalFormat("#.##").format(double)
        }
    }
}

fun isPositiveResponseHttpCode(code: Int): Boolean {
    // The positive http code is in format of 2xx
    return (code - 200 >= 0) && (code - 300 < 0)
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun getDbMigration1to2(): Migration {
    return object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `card_payment` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " `cardId` TEXT NOT NULL," +
                    " `productId` TEXT NOT NULL," +
                    " `value` REAL NOT NULL," +
                    " `createdAt` TEXT NOT NULL)"
            )
        }
    }
}

fun hideKeyboard(context: Context) {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
}
