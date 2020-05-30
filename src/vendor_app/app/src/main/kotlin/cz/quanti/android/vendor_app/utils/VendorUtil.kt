package cz.quanti.android.vendor_app.utils

import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import quanti.com.kotlinlog.Log
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
            try {
                database.execSQL("DROP TABLE IF EXISTS `selected_product`")

                val vouchers = database.query("SELECT * FROM `voucher`")
                vouchers.use {
                    if (it.moveToFirst()) {
                        val values = ContentValues()
                        values.put("id", vouchers.getLong(vouchers.getColumnIndex("id")))
                        values.put(
                            "booklet",
                            vouchers.getString(vouchers.getColumnIndex("booklet"))
                        )
                        values.put(
                            "value",
                            vouchers.getLong(vouchers.getColumnIndex("value")).toDouble()
                        )
                        values.put("usedAt", vouchers.getLong(vouchers.getColumnIndex("usedAt")))
                        values.put("quantity", 0)
                        values.put("productId", 0)
                        values.put("vendorId", 0)
                        database.execSQL("DROP TABLE IF EXISTS `voucher`")
                        createVoucherTableVersion2(database)
                        database.insert("voucher", 0, values)
                    } else {
                        database.execSQL("DROP TABLE IF EXISTS `voucher`")
                        createVoucherTableVersion2(database)
                    }
                }
            } catch (e: Exception) {
                Log.e(e)
            }
        }
    }
}

private fun createVoucherTableVersion2(database: SupportSQLiteDatabase) {
    database.execSQL(
        """CREATE TABLE IF NOT EXISTS `voucher` (
                            `dbId` INTEGER NOT NULL,
                            `id` INTEGER NOT NULL,
                            `booklet` TEXT NOT NULL,
                            `productId` INTEGER NOT NULL,
                            `quantity` REAL NOT NULL,
                            `usedAt` INTEGER NOT NULL,
                            `value` REAL NOT NULL,
                            `vendorId` INTEGER NOT NULL,
                            PRIMARY KEY(`dbId`))""".trimIndent()
    )
}
