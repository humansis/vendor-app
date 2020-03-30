package cz.quanti.android.vendor_app.utils

class LoginManager {
    private var user: Pair<String, String>? = null

    fun login(username: String, saltedPassword: String) {
        user = Pair(username, saltedPassword)
    }

    fun getAuthHeader(): String? {
        user?.let { user ->
            val username = user.first
            val saltedPassword = user.second
            return generateXWSSEHeader(
                username, saltedPassword, true
            )
        }
        return null
    }
}
