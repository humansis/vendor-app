package cz.quanti.android.vendor_app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.utils.Constants
import cz.quanti.android.vendor_app.utils.OnTagDiscoveredEnum
import cz.quanti.android.vendor_app.utils.PermissionRequestResult
import cz.quanti.android.vendor_app.utils.SingleLiveEvent
import io.reactivex.BackpressureStrategy

class MainViewModel(
    private val syncFacade: SynchronizationFacade
) : ViewModel() {

    private var nfcAdapter:  NfcAdapter? = null
    private var onTagDiscovered: OnTagDiscoveredEnum? = null

    val tagForPaymentDiscoveredSLE = SingleLiveEvent<Tag>()
    val cameraPermissionsGrantedSLE = SingleLiveEvent<PermissionRequestResult>()

    private val toastMessageLD = MutableLiveData<String?>(null)

    fun initNfcAdapter(context: Context){
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (nfcAdapter == null) {
            setToastMessage(context.getString(R.string.no_nfc_available))
        }
    }

    fun enableNfc(activity: Activity, onTagDiscovered: OnTagDiscoveredEnum?): Boolean {
        nfcAdapter?.let { adapter ->
            return if (!adapter.isEnabled) {
                showWirelessSettings(activity)
                false
            } else {
                this.onTagDiscovered = onTagDiscovered
                onTagDiscovered?.let {
                    adapter.enableReaderMode(
                        activity,
                        activity as MainActivity,
                        FLAGS,
                        null
                    )
                }
                true
            }
        }
        return false
    }

    fun disableNfc(activity: Activity) {
        onTagDiscovered = null
        nfcAdapter?.disableReaderMode(activity)
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

    fun getOnTagDiscovered(): OnTagDiscoveredEnum? {
        return onTagDiscovered
    }

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

    fun setToastMessage(message: String?) {
        toastMessageLD.value = message
    }

    fun getToastMessage(): LiveData<String?> {
        return toastMessageLD
    }

    fun onSuccess(context: Context) {
        vibrate(context)
        MediaPlayer.create(context, R.raw.end).start()
    }

    fun onError(context: Context) {
        vibrate(context)
        MediaPlayer.create(context, R.raw.error).start()
    }

    @Suppress("DEPRECATION")
    private fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
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
