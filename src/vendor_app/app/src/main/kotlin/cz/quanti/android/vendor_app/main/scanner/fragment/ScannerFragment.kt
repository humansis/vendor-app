package cz.quanti.android.vendor_app.main.scanner.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.scanner.viewmodel.ScannerViewModel
import kotlinx.android.synthetic.main.fragment_scanner.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log
import java.util.*
import kotlin.concurrent.timerTask

class ScannerFragment: Fragment() {

        private val vm: ScannerViewModel by viewModel()
        private  var codeScanner: CodeScanner? = null
        private var lastScanned: String = ""
        private var clearCachedTimer: Timer = Timer()
        val CAMERA_PERMISSION_REQUEST_CODE = 1230

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            (activity as MainActivity).supportActionBar?.show()
            return inflater.inflate(R.layout.fragment_scanner, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            if (!cameraPermissionGranted())
            {
                requestPermissions(arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            } else {
                runScanner()
            }

        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {

            if(requestCode == CAMERA_PERMISSION_REQUEST_CODE)
            {
                for (i in permissions.indices) {
                    if(permissions[i].equals(Manifest.permission.CAMERA))
                    {
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        {
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
            if (ContextCompat.checkSelfPermission(activity as MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
                    if(lastScanned != it.text)
                    {
                        try {
                            clearCachedTimer.cancel()
                        } catch(e: Exception) {
                            Log.d(e)
                        }
                        clearCachedTimer = Timer()


                        lastScanned = it.text

                        //TODO process code here

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
            super.onDestroy()
        }
}
