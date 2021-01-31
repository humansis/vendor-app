package cz.quanti.android.vendor_app.utils

import android.net.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

fun getStringFromDouble(double: Double): String {
    val abs = abs(double)
    return when {
        abs % 1.0 < 0.001 -> {
            DecimalFormat("#,###").format(double)
        }
        (abs * 10) % 1.0 < 0.01 -> {
            DecimalFormat("#,###.#").format(double)
        }
        else -> {
            DecimalFormat("#,###.##").format(double)
        }
    }
}

fun isPositiveResponseHttpCode(code: Int): Boolean {
    // The positive http code is in format of 2xx
    return (code - 200 >= 0) && (code - 300 < 0)
}


fun convertTimeForApiRequestBody(date: Date): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        .format(date)
}

fun getDefaultCurrency(country: String): String {
    return when (country) {
        "KHM" -> "KHR"
        "SYR" -> "SYP"
        "UKR" -> "UAH"
        "ARM" -> "AMD"
        "MNG" -> "MNT"
        "ETH" -> "ETB"
        else -> ""
    }
}

fun round(value: Double, places: Int): Double {
    require(places >= 0)
    var bd: BigDecimal = BigDecimal.valueOf(value)
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}
