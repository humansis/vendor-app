package cz.quanti.android.vendor_app.utils

import android.util.Base64
import java.security.MessageDigest

fun hashSHA1(s: String): String {
    return Base64.encodeToString(
        MessageDigest.getInstance("SHA-1").digest(s.toByteArray()),
        Base64.NO_WRAP
    )
}
