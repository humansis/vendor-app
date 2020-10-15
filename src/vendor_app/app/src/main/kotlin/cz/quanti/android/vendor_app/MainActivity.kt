package cz.quanti.android.vendor_app

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.findNavController
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import extensions.isNetworkConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.custom_action_bar.*
import org.koin.android.ext.android.inject
import quanti.com.kotlinlog.Log
import java.util.*


class MainActivity : AppCompatActivity(), ActivityCallback {

    private val loginFacade: LoginFacade by inject()
    private val syncFacade: SynchronizationFacade by inject()
    private val preferences: AppPreferences by inject()
    private val nfcTagPublisher: NfcTagPublisher by inject()
    private var disposable: Disposable? = null
    private var syncDisposable: Disposable? = null

    var vendorFragmentCallback: VendorFragmentCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setCustomView(R.layout.custom_action_bar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            supportActionBar?.setBackgroundDrawable(getColor(R.color.lightGrey).toDrawable())
        } else {
            supportActionBar?.setBackgroundDrawable(resources.getColor(R.color.lightGrey).toDrawable())
        }
        setContentView(R.layout.activity_main)
    }

    override fun onDestroy() {
        disposable?.dispose()
        syncDisposable?.dispose()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val leftMenu: View? = findViewById(R.id.left_menu)
        leftMenu?.setOnClickListener {
            showPopupMenu(it)
        }

        disposable?.dispose()
        disposable = syncFacade.isSyncNeeded()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if(it) {
                        dot?.visibility = View.VISIBLE
                    } else {
                        dot?.visibility = View.INVISIBLE
                    }
                },
                {
                }
            )

        syncButton?.setOnClickListener { view ->
            progressBar?.visibility = View.VISIBLE
            syncButtonArea?.visibility = View.INVISIBLE

            syncDisposable?.dispose()
            syncDisposable = syncFacade.synchronize()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        progressBar?.visibility = View.GONE
                        syncButtonArea?.visibility = View.VISIBLE
                        preferences.lastSynced = Date().time
                        vendorFragmentCallback?.notifyDataChanged()

                        dot?.visibility = View.INVISIBLE
                        Toast.makeText(
                            this,
                            getString(R.string.data_were_successfully_synchronized),
                            Toast.LENGTH_LONG
                        ).show()

                    },
                    { e ->
                        progressBar?.visibility = View.GONE
                        syncButtonArea?.visibility = View.VISIBLE
                        Log.e(e)

                        if (!isNetworkConnected()) {
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
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action || NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            nfcTagPublisher.getTagSubject().onNext(tag)
        }
    }

    private fun showPopupMenu(v: View) {
        PopupMenu(this, v).apply {
            setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item?.itemId) {

                        R.id.logoutButton -> {
                            logout()
                            true
                        }
                        else -> false
                    }
                }

            })
            inflate(R.menu.action_bar_left_actions)
            show()
        }
    }

    private fun logout() {
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
    }

    override fun onResume() {
        super.onResume()
        val toolbar = supportActionBar
        toolbar?.title = getString(R.string.vendor_title)
        toolbar?.setDisplayHomeAsUpEnabled(false)
        toolbar?.setDisplayShowTitleEnabled(true)
        toolbar?.setDisplayShowCustomEnabled(true)

        invalidateOptionsMenu()
    }

    override fun showDot(boolean: Boolean) {
        if(boolean) {
            dot?.visibility = View.VISIBLE
        } else {
            dot?.visibility = View.INVISIBLE
        }
    }
}
