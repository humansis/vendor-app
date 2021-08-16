package cz.quanti.android.vendor_app

import android.content.Context
import android.media.MediaPlayer
import android.nfc.NfcAdapter
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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

    val cameraPermissionsGrantedSLE = SingleLiveEvent<PermissionRequestResult>()

    private val toastMessageLD = MutableLiveData<String?>(null)

    fun setNfcAdapter(nfcAdapter: NfcAdapter?) {
        this.nfcAdapter = nfcAdapter
    }

    fun getNfcAdapter(): NfcAdapter? {
        return nfcAdapter
    }

    fun setOnTagDiscovered(onTagDiscovered: OnTagDiscoveredEnum?) {
        this.onTagDiscovered = onTagDiscovered
    }

    fun getOnTagDiscovered(): OnTagDiscoveredEnum? {
        return onTagDiscovered
    }

    fun showDot(): LiveData<Boolean> {
        return syncFacade.getPurchasesCount().flatMap { purchasesCount ->
            syncFacade.isSyncNeeded(purchasesCount).toObservable()
        }.toFlowable(BackpressureStrategy.LATEST).toLiveData()
    }

    fun grantPermission(permissionResult: PermissionRequestResult) {
        when (permissionResult.requestCode) {
            Constants.CAMERA_PERMISSION_REQUEST_CODE -> {
                cameraPermissionsGrantedSLE.value = permissionResult
            }
        }
    }

    fun setToastMessage(message: String?) {
        toastMessageLD.value = message
    }

    fun getToastMessage(): LiveData<String?> {
        return toastMessageLD
    }

    fun onSucces(context: Context) {
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
}
