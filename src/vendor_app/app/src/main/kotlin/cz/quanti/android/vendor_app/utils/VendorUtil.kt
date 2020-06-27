package cz.quanti.android.vendor_app.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

fun getStringFromDouble(double: Double): String {
    val abs = abs(double)
    return when {
        abs % 1.0 < 0.001 -> {
            double.roundToInt().toString()
        }
        (abs * 10) % 1.0 < 0.01 -> {
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

fun hideKeyboard(context: Context) {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
}

fun convertTimeForApiRequestBody(date: Date): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        .format(date)
}

fun getDbMigration1to2(): Migration {
    return object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            createVoucherTableVersion2(database)
            createCardPaymentTable(database)
            changeSelectedProductsTable(database)
            createVoucherPurchaseTable(database)
        }
    }
}

private fun createCardPaymentTable(database: SupportSQLiteDatabase) {
    database.execSQL(
        "CREATE TABLE IF NOT EXISTS `card_payment` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            " `cardId` TEXT NOT NULL," +
            " `productId` TEXT NOT NULL," +
            " `value` REAL NOT NULL," +
            " `createdAt` TEXT NOT NULL)"
    )
    database.execSQL(
        "CREATE TABLE IF NOT EXISTS `blocked_smartcard` (" +
            "`id` String PRIMARY KEY NOT NULL)"
    )
}

private fun changeSelectedProductsTable(database: SupportSQLiteDatabase) {
    database.execSQL("DROP TABLE IF EXISTS `selected_product`")
    database.execSQL(
        "CREATE TABLE IF NOT EXISTS `selected_product` (" +
            "`dbId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            " `productId` INTEGER NOT NULL," +
            " `value` REAL NOT NULL," +
            " `purchaseId` INTEGER NOT NULL)"
    )
}

private fun createVoucherPurchaseTable(database: SupportSQLiteDatabase) {
    database.execSQL(
        "CREATE TABLE IF NOT EXISTS `voucher_purchase` (" +
            "`dbId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            " `vendorId` INTEGER NOT NULL," +
            " `createdAt` TEXT NOT NULL"
    )
}

private fun createVoucherTableVersion2(database: SupportSQLiteDatabase) {
    database.execSQL("DROP TABLE IF EXISTS `voucher`")
    database.execSQL(
        """CREATE TABLE IF NOT EXISTS `voucher` (
                            `dbId` INTEGER AUTOINCREMENT NOT NULL,
                            `id` INTEGER NOT NULL,
                            `purchaseId` INTEGER NOT NULL,
                            PRIMARY KEY(`dbId`))""".trimIndent()
    )
}
