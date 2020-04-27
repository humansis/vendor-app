package cz.quanti.android.vendor_app.utils

import android.content.Context
import android.net.ConnectivityManager
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
