package cz.quanti.android.vendor_app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.utils.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable

class MainViewModel(
    private val syncFacade: SynchronizationFacade,
    private val currentVendor: CurrentVendor
) : ViewModel() {

    private var nfcAdapter:  NfcAdapter? = null

    val cameraPermissionsGrantedSLE = SingleLiveEvent<PermissionRequestResult>()
    val successSLE = SingleLiveEvent<Unit>()
    val errorSLE = SingleLiveEvent<Unit>()
    val toastMessageSLE = SingleLiveEvent<String>()

    fun getCurrentEnvironment(): Observable<ApiEnvironments> {
        return currentVendor.getEnvironment()
    }

    fun initNfcAdapter(activity: Activity) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        enableNfc(activity)
    }

    fun enableNfc(activity: Activity): Boolean {
        nfcAdapter?.let { adapter ->
            return if (!adapter.isEnabled) {
                showWirelessSettings(activity)
                false
            } else {
                adapter.enableReaderMode(
                    activity,
                    activity as MainActivity,
                    FLAGS,
                    null
                )
                true
            }
        }
        setToastMessage(activity.getString(R.string.no_nfc_available))
        return false
    }

    private fun showWirelessSettings(context: Context) {
        AlertDialog.Builder(context, R.style.DialogTheme)
            .setMessage(context.getString(R.string.you_need_to_enable_nfc))
            .setCancelable(true)
            .setPositiveButton(context.getString(R.string.proceed)) { _,_ ->
                context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create()
            .show()
    }

    fun grantPermission(permissionResult: PermissionRequestResult) {
        when (permissionResult.requestCode) {
            Constants.CAMERA_PERMISSION_REQUEST_CODE -> {
                cameraPermissionsGrantedSLE.value = permissionResult
            }
        }
    }

    fun showDot(): LiveData<Boolean> {
        return syncFacade.getPurchasesCount().flatMapSingle { purchasesCount ->
            syncFacade.isSyncNeeded(purchasesCount)
        }.toFlowable(BackpressureStrategy.LATEST).toLiveData()
    }

    fun setToastMessage(message: String) {
        toastMessageSLE.postValue(message)
    }

    companion object {
        private const val FLAGS = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NFC_BARCODE or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS
    }
}
