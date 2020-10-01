package extensions

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        connectivityManager.getNetworkCapabilities(network) ?: return false
        return true
    } else {
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}
