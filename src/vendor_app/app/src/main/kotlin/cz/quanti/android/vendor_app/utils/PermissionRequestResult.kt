package cz.quanti.android.vendor_app.utils

class PermissionRequestResult(
    var requestCode: Int,
    var permissions: Array<out String>,
    var grantResults: IntArray
)
