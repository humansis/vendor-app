package cz.quanti.android.vendor_app

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.utils.Constants
import cz.quanti.android.vendor_app.utils.PermissionRequestResult
import cz.quanti.android.vendor_app.utils.SingleLiveEvent

class MainViewModel : ViewModel() {
    val cameraPermissionsGrantedSLE = SingleLiveEvent<PermissionRequestResult>()

    fun grantPermission(permissionResult: PermissionRequestResult) {
        when (permissionResult.requestCode) {
            Constants.CAMERA_PERMISSION_REQUEST_CODE -> {
                cameraPermissionsGrantedSLE.value = permissionResult
            }
        }
    }
}
