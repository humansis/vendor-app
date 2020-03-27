package cz.quanti.android.vendor_app.utils

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
