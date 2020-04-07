package cz.quanti.android.vendor_app.main.scanner.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.textfield.TextInputEditText
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.scanner.viewmodel.ScannerViewModel
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.utils.Constants
import cz.quanti.android.vendor_app.utils.hashSHA1
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_scanner.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log
import java.util.*
import kotlin.concurrent.timerTask

class ScannerFragment() : Fragment() {

    private val vm: ScannerViewModel by viewModel()
    private var codeScanner: CodeScanner? = null
    private var lastScanned: String = ""
    private var clearCachedTimer: Timer = Timer()
    private lateinit var deactivated: List<Booklet>
    private lateinit var protected: List<Booklet>
    private var disposables: MutableList<Disposable> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.show()
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disposables.add(vm.getDeactivatedAndProtectedBooklets().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    deactivated = it.first
                    protected = it.second

                    if (!cameraPermissionGranted()) {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            Constants.CAMERA_PERMISSION_REQUEST_CODE
                        )
                    } else {
                        runScanner()
                    }
                },
                {
                    Log.e(it)
                }
            ))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == Constants.CAMERA_PERMISSION_REQUEST_CODE) {
            for (i in permissions.indices) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        runScanner()
                    } else {
                        Log.d("Permission not granted")
                    }
                    break
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    private fun runScanner() {
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, fragmentScanner)
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
        codeScanner?.startPreview()
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

    override fun onDestroy() {
        codeScanner?.releaseResources()
        for (disposable in disposables) {
            disposable.dispose()
        }
        disposables.clear()
        super.onDestroy()
    }

    private fun processScannedCode(scannedCode: String) {
        val code = scannedCode.replace(" ", "+")
        if (vm.wasAlreadyScanned(code)) {
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle(getString(R.string.already_scanned_dialog_title))
                .setMessage(getString(R.string.already_scanned_dialog_message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } else {
            val result =
                vm.getVoucherFromScannedCode(scannedCode, deactivated, protected)
            val voucher = result.first
            val resultCode = result.second
            if (voucher != null &&
                (resultCode == ScannerViewModel.VOUCHER_WITH_PASSWORD ||
                    resultCode == ScannerViewModel.VOUCHER_WITHOUT_PASSWORD)
            ) {
                if (resultCode == ScannerViewModel.VOUCHER_WITH_PASSWORD) {
                    showPasswordDialog(3, voucher)

                } else {
                    vm.addVoucher(voucher)
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
            disposables.add(vm.deactivate(voucher).subscribeOn(Schedulers.io())
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
                limitedTriesTextView.text = getString(R.string.wrong_voucher_password, tries)
            }
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val passwordEditTextView =
                        dialogView.findViewById<TextInputEditText>(R.id.passwordEditText)
                    var password = hashSHA1(passwordEditTextView.text.toString())
                    if (password in voucher.passwords) {
                        vm.addVoucher(voucher)
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

    private fun getDialogMessageForResultCode(code: Int): Pair<String, String> {
        var title = getString(R.string.wrong_code_title)
        var message = ""

        when (code) {
            ScannerViewModel.BOOKLET -> {
                message = getString(R.string.cannot_scan_booklet_dialog_message)
            }
            ScannerViewModel.WRONG_FORMAT -> {
                message = getString(R.string.wrong_code_dialog_message)
            }
            ScannerViewModel.DEACTIVATED -> {
                message = getString(R.string.the_booklet_is_deactivated)
            }
            ScannerViewModel.WRONG_BOOKLET -> {
                message = getString(R.string.you_just_scanned_a_voucher_from_a_different_booklet)
            }
            ScannerViewModel.WRONG_CURRENCY -> {
                message = getString(R.string.the_voucher_is_of_wrong_currency)
            }
        }
        return Pair(title, message)
    }
}
