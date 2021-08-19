package cz.quanti.android.vendor_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.utils.Constants
import cz.quanti.android.vendor_app.utils.PermissionRequestResult
import cz.quanti.android.vendor_app.utils.SingleLiveEvent
import io.reactivex.BackpressureStrategy

class MainViewModel(
    private val syncFacade: SynchronizationFacade
) : ViewModel() {

    val cameraPermissionsGrantedSLE = SingleLiveEvent<PermissionRequestResult>()

    fun grantPermission(permissionResult: PermissionRequestResult) {
        when (permissionResult.requestCode) {
            Constants.CAMERA_PERMISSION_REQUEST_CODE -> {
                cameraPermissionsGrantedSLE.value = permissionResult
            }
        }
    }

    fun showDot(): LiveData<Boolean> {
        return syncFacade.getPurchasesCount().flatMap { purchasesCount ->
            syncFacade.isSyncNeeded(purchasesCount).toObservable()
        }.toFlowable(BackpressureStrategy.LATEST).toLiveData()
    }
}
