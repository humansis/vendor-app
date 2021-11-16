package cz.quanti.android.vendor_app.utils

import android.app.Activity
import android.content.Context
import android.nfc.Tag
import android.text.format.DateFormat.getDateFormat
import android.text.format.DateFormat.getTimeFormat
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cz.quanti.android.nfc.dto.v2.UserBalance
import cz.quanti.android.nfc_io_libray.types.NfcUtil
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

fun convertStringToDateFormattedString(context: Context, date: String): String? {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(date)
    return if (df != null) {
        "${getDateFormat(context).format(df)}  ${getTimeFormat(context).format(df)}"
    } else {
        null
    }
}

fun convertStringToDate(date: String?): Date? {
    return date?.let { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(date) }
}

fun convertDateToString(value: Date, context: Context): String {
    return getDateFormat(context).format(value)
}

fun convertTagToString(tag: Tag): String {
    return NfcUtil.toHexString(tag.id).uppercase(Locale.US)
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

fun getBackgroundColor(context: Context, environment: ApiEnvironments?): Int {
    return when (environment?.id) {
        ApiEnvironments.DEV.id -> {
            ContextCompat.getColor(context, R.color.dev)
        }
        ApiEnvironments.TEST.id -> {
            ContextCompat.getColor(context, R.color.test)
        }
        ApiEnvironments.STAGE.id -> {
            ContextCompat.getColor(context, R.color.stage)
        }
        ApiEnvironments.DEMO.id -> {
            ContextCompat.getColor(context, R.color.demo)
        }
        else -> {
            ContextCompat.getColor(context, R.color.screenBackgroundColor)
        }
    }
}

fun getExpirationDateAsString(expirationDate: Date?, context: Context): String {
    return if (expirationDate != null) {
        context.getString(
            R.string.expiration_date_formatted,
            convertDateToString(expirationDate, context)
        )
    } else {
        String()
    }
}

fun getLimitsAsText(cardContent: UserBalance, context: Context): String {
    var limits = String()
    cardContent.limits.map { entry ->
        CategoryType.getById(entry.key).stringRes?.let {
            limits += context.getString(
                R.string.product_type_limit_formatted,
                context.getString(it),
                "${entry.value} ${cardContent.currencyCode}"
            )
        }
    }
    return limits
}

fun constructLimitsExceededMessage(
    exceeded: MutableMap<Int, Double>,
    notAllowed: MutableMap<Int, Double>,
    context: Context
): String {
    var message = ""
    exceeded.forEach { entry ->
        val typeName = CategoryType.getById(entry.key).stringRes?.let { context.getString(it) }
        message += context.getString(
            R.string.commodity_type_exceeded,
            typeName,
            String.format("%.2f", entry.value)
        ) + "\n"
    }
    if (notAllowed.isNotEmpty()) {
        message += context.getString(R.string.commodity_type_not_allowed)
        notAllowed.forEach { entry ->
            val typeName = CategoryType.getById(entry.key).stringRes?.let { context.getString(it) }
            message += "\n" + typeName
        }
    }
    message += "\n\n" + context.getString(R.string.please_update_cart)
    return message
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun round(value: Double, places: Int): Double {
    require(places >= 0)
    var bd: BigDecimal = BigDecimal.valueOf(value)
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}
