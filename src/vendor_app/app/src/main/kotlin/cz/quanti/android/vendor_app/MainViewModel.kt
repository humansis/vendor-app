package cz.quanti.android.vendor_app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc.dto.v2.UserBalance
import cz.quanti.android.nfc.logger.NfcLogger
import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.utils.ApiEnvironment
import cz.quanti.android.vendor_app.utils.Constants
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import cz.quanti.android.vendor_app.utils.PermissionRequestResult
import cz.quanti.android.vendor_app.utils.SingleLiveEvent
import cz.quanti.android.vendor_app.utils.convertTagToString
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class MainViewModel(
    private val nfcFacade: VendorFacade,
    private val depositFacade: DepositFacade,
    private val currentVendor: CurrentVendor,
    private val nfcTagPublisher: NfcTagPublisher
) : ViewModel() {

    private var nfcAdapter: NfcAdapter? = null

    private var displayedDialog: AlertDialog? = null

    private val isNetworkConnectedLD = MutableLiveData<Boolean>()

    private val cameraPermissionsGrantedSLE = SingleLiveEvent<PermissionRequestResult>()
    val successSLE = SingleLiveEvent<Unit>()
    val errorSLE = SingleLiveEvent<Unit>()
    val toastMessageSLE = SingleLiveEvent<String?>()

    fun getApiHost(): ApiEnvironment? {
        return currentVendor.host
    }

    fun initNfcAdapter(activity: Activity) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        enableNfc(activity)
    }

    fun hasNfcAdapter(): Boolean {
        return nfcAdapter != null
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

        // do not use setToastMessage() here, because it won't be displayed when finishing activity.
        Toast.makeText(activity, activity.getString(R.string.no_nfc_available), Toast.LENGTH_LONG).show()
        if (!BuildConfig.DEBUG) {
            activity.finish()
        }
        return false
    }

    private fun showWirelessSettings(context: Context) {
        displayedDialog?.dismiss()
        displayedDialog = AlertDialog.Builder(context, R.style.DialogTheme)
            .setMessage(context.getString(R.string.you_need_to_enable_nfc))
            .setCancelable(true)
            .setPositiveButton(context.getString(R.string.proceed)) { _, _ ->
                context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }

    fun grantPermission(permissionResult: PermissionRequestResult) {
        when (permissionResult.requestCode) {
            Constants.CAMERA_PERMISSION_REQUEST_CODE -> {
                cameraPermissionsGrantedSLE.value = permissionResult
            }
        }
    }

    fun isNetworkConnected(available: Boolean) {
        isNetworkConnectedLD.value = available
    }

    fun isNetworkConnected(): LiveData<Boolean> {
        return isNetworkConnectedLD
    }

    fun setToastMessage(message: String?) {
        toastMessageSLE.postValue(message)
    }

    fun readBalance(readBalanceStarted: () -> Unit): Single<UserBalance> {
        return nfcTagPublisher.getTagObservable().firstOrError().flatMap { tag ->
            readBalanceStarted.invoke()
            depositFacade.getRelevantReliefPackage(convertTagToString(tag))
                .subscribeOn(Schedulers.io())
                .flatMap { wrappedReliefPackage ->
                    val deposit = wrappedReliefPackage.nullableObject?.convertToDeposit()
                    nfcFacade.readUserBalance(tag)
                        .flatMap { userBalance ->
                            NfcLogger.d(TAG, "readUserBalance: $userBalance, reliefPackage: $deposit")
                            if (deposit != null && deposit.beneficiaryId == userBalance.userId && deposit.assistanceId != userBalance.assistanceId) {
                                Single.just(
                                    UserBalance(
                                        userId = deposit.beneficiaryId,
                                        assistanceId = deposit.assistanceId,
                                        expirationDate = deposit.expirationDate,
                                        currencyCode = deposit.currency,
                                        originalBalance = userBalance.balance,
                                        balance = deposit.amount,
                                        limits = deposit.limits,
                                        depositDone = false
                                    )
                                )
                            } else {
                                Single.just(userBalance)
                            }
                        }
                }
        }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
        private const val FLAGS = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NFC_BARCODE or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS
    }
}
