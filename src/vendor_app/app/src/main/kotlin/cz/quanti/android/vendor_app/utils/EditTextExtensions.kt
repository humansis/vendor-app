package cz.quanti.android.vendor_app.utils

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.widget.EditText
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

// extension function to input filter edit text decimal digits
fun EditText.inputFilterDecimal(
    // maximum digits including point and without decimal places
    maxDigitsIncludingPoint: Int,
    maxDecimalPlaces: Int // maximum decimal places
) {
    try {
        filters = arrayOf<InputFilter>(
            DecimalDigitsInputFilter(maxDigitsIncludingPoint, maxDecimalPlaces)
        )
    } catch (e: PatternSyntaxException) {
        isEnabled = false
        hint = e.message
    }
}

// class to decimal digits input filter
class DecimalDigitsInputFilter(digitsBeforeZero: Int?, digitsAfterZero: Int?) :
    InputFilter {
    private val mDigitsBeforeZero: Int = digitsBeforeZero ?: DIGITS_BEFORE_ZERO_DEFAULT
    private val mDigitsAfterZero: Int = digitsAfterZero ?: DIGITS_AFTER_ZERO_DEFAULT
    private val mPattern: Pattern = Pattern.compile(
        "-?[0-9]{0," + mDigitsBeforeZero + "}+((\\.[0-9]{0," + mDigitsAfterZero +
            "})?)||(\\.)?"
    )

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val replacement = source.subSequence(start, end).toString()
        val newVal = (dest.subSequence(0, dstart).toString() + replacement +
            dest.subSequence(dend, dest.length).toString())
        val matcher = mPattern.matcher(newVal)
        if (matcher.matches()) return null
        return if (TextUtils.isEmpty(source)) dest.subSequence(dstart, dend) else ""
    }

    companion object {
        private const val DIGITS_BEFORE_ZERO_DEFAULT = 100
        private const val DIGITS_AFTER_ZERO_DEFAULT = 100
    }
}
