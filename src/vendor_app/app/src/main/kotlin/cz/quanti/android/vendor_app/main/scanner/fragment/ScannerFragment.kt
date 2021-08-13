package cz.quanti.android.vendor_app.main.scanner.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.app.AlertDialog
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.textfield.TextInputEditText
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.MainViewModel
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.FragmentScannerBinding
import cz.quanti.android.vendor_app.main.scanner.ScannedVoucherReturnState
import cz.quanti.android.vendor_app.main.scanner.viewmodel.ScannerViewModel
import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher
import cz.quanti.android.vendor_app.utils.Constants
import cz.quanti.android.vendor_app.utils.hashSHA1
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log
import java.util.*
import kotlin.concurrent.timerTask

class ScannerFragment : Fragment() {

    private val mainVM: MainViewModel by sharedViewModel()
    private val scannerVM: ScannerViewModel by viewModel()
    private var codeScanner: CodeScanner? = null
    private var lastScanned: String = ""
    private var clearCachedTimer: Timer = Timer()
    private lateinit var deactivated: List<Booklet>
    private lateinit var protected: List<Booklet>
    private var disposables: MutableList<Disposable> = mutableListOf()
    private lateinit var activityCallback: ActivityCallback

    private lateinit var scannerBinding: FragmentScannerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = requireActivity() as ActivityCallback
        activityCallback.setSubtitle(null)
        activityCallback.setDrawerLocked(true)
        activityCallback.setSyncButtonEnabled(false)
        scannerBinding = FragmentScannerBinding.inflate(inflater, container, false)
        return scannerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disposables.add(scannerVM.getDeactivatedAndProtectedBooklets().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    deactivated = it.first
                    protected = it.second

                    startScanner()
                },
                {
                    Log.e(it)
                }
            ))
    }

    override fun onResume() {
        super.onResume()
        codeScanner?.startPreview()
    }

    override fun onPause() {
        codeScanner?.releaseResources()
        super.onPause()
    }

    override fun onStop() {
        codeScanner?.releaseResources()
        super.onStop()
    }

    override fun onDestroyView() {
        activityCallback.setDrawerLocked(false)
        activityCallback.setSyncButtonEnabled(true)
        super.onDestroyView()
    }

    override fun onDestroy() {
        codeScanner?.releaseResources()
        for (disposable in disposables) {
            disposable.dispose()
        }
        disposables.clear()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.CAMERA_PERMISSION_REQUEST_CODE) {
            onCameraPermissionsResult(permissions, grantResults)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onCameraPermissionsResult(permissions: Array<out String>, grantResults: IntArray) {
        permissions.firstOrNull { it == Manifest.permission.CAMERA }?.let {
            val index = permissions.indexOf(it)
            if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    runScanner()
                } else {
                    Log.d("Permission not granted")
                }
        }
    }

    /*
    * check if permission is granted
    **/
    private fun cameraPermissionGranted(): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity as MainActivity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    private fun startScanner() {
        if (!cameraPermissionGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mainVM.cameraPermissionsGrantedSLE.observe(viewLifecycleOwner, { permissionResult ->
                    onCameraPermissionsResult(permissionResult.permissions, permissionResult.grantResults)
                })
                requireActivity().requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    Constants.CAMERA_PERMISSION_REQUEST_CODE
                )
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    Constants.CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            runScanner()
        }
    }

    private fun runScanner() {
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerBinding.fragmentScanner)
        codeScanner?.scanMode = ScanMode.CONTINUOUS
        codeScanner?.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                if (lastScanned != it.text) {
                    try {
                        clearCachedTimer.cancel()
                    } catch (e: Exception) {
                        Log.d(e)
                    }
                    clearCachedTimer = Timer()
                    lastScanned = it.text
                    processScannedCode(it.text)
                    clearCachedTimer.schedule(timerTask {
                        lastScanned = ""
                    }, 5000)
                }
            }
        }
        Timer().schedule(timerTask {
            codeScanner?.startPreview()
        }, DEFAULT_ANIMATION_LENGTH)

    }

    private fun processScannedCode(scannedCode: String) {
        if (scannerVM.wasAlreadyScanned(scannedCode)) {
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle(getString(R.string.already_scanned_dialog_title))
                .setMessage(getString(R.string.already_scanned_dialog_message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } else {
            val result =
                scannerVM.getVoucherFromScannedCode(scannedCode, deactivated, protected)
            val voucher = result.first
            val resultCode = result.second
            if (voucher != null &&
                (resultCode == ScannedVoucherReturnState.VOUCHER_WITH_PASSWORD ||
                    resultCode == ScannedVoucherReturnState.VOUCHER_WITHOUT_PASSWORD)
            ) {
                if (resultCode == ScannedVoucherReturnState.VOUCHER_WITH_PASSWORD) {
                    showPasswordDialog(3, voucher)

                } else {
                    scannerVM.addVoucher(voucher)
                    findNavController().navigate(
                        ScannerFragmentDirections.actionScannerFragmentToCheckoutFragment()
                    )
                }
            } else {
                val message = getDialogMessageForResultCode(resultCode)
                AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                    .setTitle(message.first)
                    .setMessage(message.second)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }
    }

    private fun showPasswordDialog(tries: Int, voucher: Voucher) {
        if (tries < 1) {
            disposables.add(scannerVM.deactivate(voucher).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                        .setTitle(getString(R.string.booklet_deactivated))
                        .setMessage(getString(R.string.tries_exceeded_booklet_deactivated))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                    findNavController().navigate(
                        ScannerFragmentDirections.actionScannerFragmentToCheckoutFragment()
                    )
                },
                {
                    Log.e(it)
                }
            ))
        } else {
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_voucher_password, null)
            val limitedTriesTextView = dialogView.findViewById<TextView>(R.id.limitedTriesTextView)
            if (tries == 3) {
                limitedTriesTextView.text = getString(R.string.limited_tries_text)
            } else {
                if(tries > 1)
                {
                    limitedTriesTextView.text = getString(R.string.wrong_voucher_password_plural, tries)
                } else {
                    limitedTriesTextView.text = getString(R.string.wrong_voucher_password, tries)
                }
            }
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val passwordEditTextView =
                        dialogView.findViewById<TextInputEditText>(R.id.passwordEditText)
                    val password = hashSHA1(passwordEditTextView.text.toString())
                    if (password in voucher.passwords) {
                        scannerVM.addVoucher(voucher)
                        findNavController().navigate(
                            ScannerFragmentDirections.actionScannerFragmentToCheckoutFragment()
                        )
                    } else {
                        showPasswordDialog(tries - 1, voucher)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun getDialogMessageForResultCode(code: ScannedVoucherReturnState): Pair<String, String> {
        val title = getString(R.string.wrong_code_title)
        var message = ""

        when (code) {
            ScannedVoucherReturnState.BOOKLET -> {
                message = getString(R.string.cannot_scan_booklet_dialog_message)
            }
            ScannedVoucherReturnState.WRONG_FORMAT -> {
                message = getString(R.string.wrong_code_dialog_message)
            }
            ScannedVoucherReturnState.DEACTIVATED -> {
                message = getString(R.string.the_booklet_is_deactivated)
            }
            ScannedVoucherReturnState.WRONG_BOOKLET -> {
                message = getString(R.string.you_just_scanned_a_voucher_from_a_different_booklet)
            }
            ScannedVoucherReturnState.WRONG_CURRENCY -> {
                message = getString(R.string.the_voucher_is_of_wrong_currency)
            }
        }
        return Pair(title, message)
    }

    companion object {
        const val DEFAULT_ANIMATION_LENGTH: Long = 300
    }
}
