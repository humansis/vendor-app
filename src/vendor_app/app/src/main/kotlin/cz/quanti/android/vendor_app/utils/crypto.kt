package cz.quanti.android.vendor_app.utils

import android.annotation.SuppressLint
import android.util.Base64
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

fun hashAndSaltPassword(salt: String, password: String): String {
    val salted = "$password{$salt}".toByteArray()
    var digest = hashSHA512(salted)

    for (i in 1..4999) {
        digest =
            hashSHA512(digest.plus(salted))
    }

    return Base64.encodeToString(digest, Base64.NO_WRAP)
}

@SuppressLint("SimpleDateFormat")
fun generateXWSSEHeader(username: String, saltedPassword: String, test: Boolean): String {

    var nonce = generateNonce()
    while (nonce == LastNonce.nonce) {
        nonce = generateNonce()
    }
    LastNonce.nonce = nonce

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val createdAt = sdf.format(Date())

    val digest = generateDigest(
        saltedPassword,
        nonce,
        createdAt
    )
    val nonce64 = Base64.encodeToString(nonce.toByteArray(), Base64.NO_WRAP)

    return "UsernameToken Username=\"$username\", PasswordDigest=\"$digest\", Nonce=\"$nonce64\", Created=\"$createdAt\""
}

fun generateDigest(saltedPassword: String, nonce: String, created: String): String {
    val mix = nonce + created + saltedPassword
    return hashSHA1(mix)
}

fun hashSHA512(input: ByteArray, iterations: Int = 1): ByteArray {
    val digestor = MessageDigest.getInstance("SHA-512")

    var result = input
    for (i in 0 until iterations) {
        result = digestor.digest(result)
    }

    return result
}

fun generateNonce(): String {
    val nonceChars = "0123456789abcdef"
    val nonce = StringBuilder()

    for (i in 0..15) {
        nonce.append(nonceChars[Random.nextInt(nonceChars.length)])
    }

    return nonce.toString()
}

fun hashSHA1(s: String): String {
    return Base64.encodeToString(
        MessageDigest.getInstance("SHA-1").digest(s.toByteArray()),
        Base64.NO_WRAP
    )
}

object LastNonce {
    var nonce: String = ""
}
