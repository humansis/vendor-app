package cz.quanti.android.vendor_app

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.Menu
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import cz.quanti.android.vendor_app.utils.isNetworkAvailable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import quanti.com.kotlinlog.Log
import java.util.*


class MainActivity : AppCompatActivity() {

    private val loginFacade: LoginFacade by inject()
    private val syncFacade: SynchronizationFacade by inject()
    private val preferences: AppPreferences by inject()
    private val nfcTagPublisher: NfcTagPublisher by inject()

    var vendorFragmentCallback: VendorFragmentCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_actions, menu)
        menu?.findItem(R.id.logoutButton)?.setOnMenuItemClickListener {
            AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(getString(R.string.are_you_sure_dialog_title))
                .setMessage(getString(R.string.logout_dialog_message))
                .setPositiveButton(
                    android.R.string.yes
                ) { _, _ ->
                    loginFacade.logout()
                    findNavController(R.id.main_nav_host).popBackStack(R.id.loginFragment, false)
                }
                .setNegativeButton(android.R.string.no, null)
                .show()

            true
        }

        val syncButton = menu?.findItem(R.id.syncButton)?.actionView as ImageView
        syncButton.setImageResource(R.drawable.sync)
        syncButton.setOnClickListener { view ->
            val animation = RotateAnimation(0f, 360f, view.width / 2f, view.height / 2f)
            animation.duration = 2500
            animation.repeatCount = Animation.INFINITE
            view.startAnimation(animation)

            syncFacade.synchronize()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        preferences.lastSynced = Date().time
                        vendorFragmentCallback?.notifyDataChanged()
                        animation.repeatCount = 0
                        Toast.makeText(
                            this,
                            getString(R.string.data_were_successfully_synchronized),
                            Toast.LENGTH_LONG
                        ).show()

                    },
                    { e ->
                        view.animation.repeatCount = 0
                        Log.e(e)

                        if (!isNetworkAvailable(this)) {
                            Toast.makeText(
                                this,
                                getString(R.string.no_internet_connection),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.could_not_synchronize_data_with_server),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            nfcTagPublisher.getTagSubject().onNext(tag)
        }
    }
}
