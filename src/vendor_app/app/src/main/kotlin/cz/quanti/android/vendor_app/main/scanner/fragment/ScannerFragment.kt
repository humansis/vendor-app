package cz.quanti.android.vendor_app.main.scanner.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.scanner.viewmodel.ScannerViewModel
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.utils.Constants
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
    private lateinit var chosenCurrency: String
    private lateinit var deactivated: List<Booklet>
    private var disposable: Disposable? = null

    val args: ScannerFragmentArgs by navArgs()

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

        chosenCurrency = args.currency
        disposable = vm.getDeactivatedBooklets().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(
            {
                deactivated = it
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
        )
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
        disposable?.dispose()
        disposable = null
        super.onDestroy()
    }

    private fun processScannedCode(scannedCode: String) {
        val code = scannedCode.replace(" ", "+")
        if (alreadyScanned(code)) {
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle(getString(R.string.already_scanned_dialog_title))
                .setMessage(getString(R.string.already_scanned_dialog_message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } else {
            var booklet = ""
            if ((activity as MainActivity).vouchers.size > 0) {
                booklet = (activity as MainActivity).vouchers[0].booklet
            }
            val result =
                vm.getVoucherFromScannedCode(scannedCode, chosenCurrency, booklet, deactivated)
            val voucher = result.first
            val resultCode = result.second
            if (voucher != null &&
                (resultCode == ScannerViewModel.VOUCHER_WITH_PASSWORD ||
                    resultCode == ScannerViewModel.VOUCHER_WITHOUT_PASSWORD)
            ) {
                (activity as MainActivity).vouchers.add(voucher)
                findNavController().navigate(
                    ScannerFragmentDirections.actionScannerFragmentToCheckoutFragment(chosenCurrency)
                )
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
        }
        return Pair(title, message)
    }

    private fun alreadyScanned(code: String): Boolean {
        for (voucher in (activity as MainActivity).vouchers) {
            if (voucher.qrCode == code) {
                return true
            }
        }
        return false
    }
}
