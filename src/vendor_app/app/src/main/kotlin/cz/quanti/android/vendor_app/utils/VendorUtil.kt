package cz.quanti.android.vendor_app.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
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
