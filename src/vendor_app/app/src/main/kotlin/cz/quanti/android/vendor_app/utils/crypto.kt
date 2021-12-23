package cz.quanti.android.vendor_app.utils

import android.util.Base64
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class Payload(
    val iat: Long,
    val exp: Long,
    val roles: List<String>,
    val username: String
)

fun getPayload(token: String): Payload {
    return json.decodeFromString(getJson(token.split(".")[1]))
}

private fun getJson(strEncoded: String): String {
    val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
    return String(decodedBytes, Charset.forName("UTF-8"))
}

fun hashSHA1(s: String): String {
    return Base64.encodeToString(
        MessageDigest.getInstance("SHA-1").digest(s.toByteArray()),
        Base64.NO_WRAP
    )
}
