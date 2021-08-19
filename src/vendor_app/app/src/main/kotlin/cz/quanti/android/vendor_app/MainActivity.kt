package cz.quanti.android.vendor_app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc.dto.UserBalance
import cz.quanti.android.vendor_app.databinding.ActivityMainBinding
import cz.quanti.android.vendor_app.databinding.NavHeaderBinding
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.shop.adapter.CurrencyAdapter
import cz.quanti.android.vendor_app.main.shop.viewmodel.ShopViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import cz.quanti.android.vendor_app.utils.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class MainActivity : AppCompatActivity(), ActivityCallback,
    NavigationView.OnNavigationItemSelectedListener {

    private val loginFacade: LoginFacade by inject()
    private val nfcFacade: VendorFacade by inject()
    private val nfcTagPublisher: NfcTagPublisher by inject()
    private val synchronizationManager: SynchronizationManager by inject()
    private val preferences: AppPreferences by inject()
    private val mainVM: MainViewModel by viewModel()
    private val loginVM: LoginViewModel by viewModel()
    private val shopVM: ShopViewModel by viewModel()
    private var displayedDialog: AlertDialog? = null
    private var disposable: Disposable? = null
    private var connectionDisposable: Disposable? = null
    private var syncStateDisposable: Disposable? = null
    private var syncDisposable: Disposable? = null
    private var readBalanceDisposable: Disposable? = null

    private lateinit var activityBinding: ActivityMainBinding
    private lateinit var navHeaderBinding: NavHeaderBinding

    private lateinit var connectionObserver: ConnectionObserver

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!this.resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        navHeaderBinding = NavHeaderBinding.bind(activityBinding.navView.getHeaderView(0))

        setContentView(activityBinding.root)

        connectionObserver = ConnectionObserver(this)
        connectionObserver.registerCallback()

        setUpToolbar()
        setUpNavigationMenu()
    }

    override fun onResume() {
        super.onResume()
        loadNavHeader(loginVM.getCurrentVendorName())
        checkConnection()
        syncState()
    }

    override fun onPause() {
        super.onPause()
        syncStateDisposable?.dispose()
    }

    override fun onStop() {
        displayedDialog?.dismiss()
        disposable?.dispose()
        syncDisposable?.dispose()
        readBalanceDisposable?.dispose()
        super.onStop()
    }

    override fun onDestroy() {
        connectionObserver.unregisterCallback()
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_button -> {
                findNavController(R.id.nav_host_fragment).popBackStack(R.id.productsFragment, false)
            }
            R.id.transactions_button -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.transactionsFragment)
            }
            R.id.invoices_button -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.invoicesFragment)
            }
            R.id.read_balance_button -> {
                showReadBalanceDialog()
            }
            R.id.share_logs_button -> {
                shareLogsDialog()
            }
            R.id.fix_card_button -> {
                // TODO fixovani karty z db + overeni casu a userid
                // TODO dat pryc pokud se zrusi db
            }
        }
        activityBinding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (activityBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            activityBinding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setUpToolbar() {
        activityBinding.navView.setNavigationItemSelectedListener(this)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.productsFragment,
                R.id.transactionsFragment,
                R.id.invoicesFragment,
                R.id.checkoutFragment
            ),
            activityBinding.drawerLayout
        )

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        activityBinding.appBar.toolbar.setupWithNavController(navHostFragment.navController, appBarConfiguration)

        mainVM.showDot().observe(this, {
            if (it) {
                activityBinding.appBar.dot.visibility = View.VISIBLE
            } else {
                activityBinding.appBar.dot.visibility = View.INVISIBLE
            }
        })

        loginVM.isNetworkConnected().observe(this, { available ->
            val drawable = if (available) R.drawable.ic_cloud else R.drawable.ic_cloud_offline
            activityBinding.appBar.syncButton.setImageDrawable(
                ContextCompat.getDrawable(this, drawable)
            )
        })

        activityBinding.appBar.syncButton.setOnClickListener {
            synchronizationManager.synchronizeWithServer()
        }
    }

    private fun getToobarUpButton(): ImageButton? {
        val field = Class.forName("androidx.appcompat.widget.Toolbar").getDeclaredField("mNavButtonView")
        field.isAccessible = true
        return field.get(activityBinding.appBar.toolbar) as? ImageButton
    }

    private fun setUpNavigationMenu() {
        initPriceUnitSpinner()
        activityBinding.btnLogout.setOnClickListener {
            logout()
            activityBinding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action || NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: return
            nfcTagPublisher.getTagSubject().onNext(tag)
        }
    }

    private fun logout() {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle(getString(R.string.are_you_sure_dialog_title))
            .setMessage(getString(R.string.logout_dialog_message))
            .setPositiveButton(
                android.R.string.ok
            ) { _, _ ->
                loginFacade.logout()
                findNavController(R.id.nav_host_fragment).popBackStack(R.id.loginFragment, false)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showReadBalanceDialog() {
        if (NfcInitializer.initNfc(this)) {
            val scanCardDialog = AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage(getString(R.string.scan_card))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog?.dismiss()
                    readBalanceDisposable?.dispose()
                    readBalanceDisposable = null
                }
                .create()

            scanCardDialog?.show()
            displayedDialog = scanCardDialog

            readBalanceDisposable?.dispose()
            readBalanceDisposable = readBalance()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    scanCardDialog.dismiss()
                    val cardContent = it
                    val cardResultDialog = AlertDialog.Builder(this, R.style.DialogTheme)
                        .setTitle(getString((R.string.read_balance)))
                        .setMessage(
                            getString(
                                R.string.scanning_card_balance,
                                "${cardContent.balance} ${cardContent.currencyCode}"
                            )
                        )
                        .setCancelable(true)
                        .setNegativeButton(getString(R.string.close)) { dialog, _ ->
                            dialog?.dismiss()
                            readBalanceDisposable?.dispose()
                            readBalanceDisposable = null
                        }
                        .create()
                    cardResultDialog.show()
                    displayedDialog = cardResultDialog
                },
                    {
                        Log.e(this.javaClass.simpleName, it)
                        Toast.makeText(
                            this,
                            getString(R.string.card_error),
                            Toast.LENGTH_LONG
                        ).show()
                        scanCardDialog.dismiss()
                        NfcInitializer.disableForegroundDispatch(this)
                    })
        }
    }

    private fun readBalance(): Single<UserBalance> {
        return nfcTagPublisher.getTagObservable().firstOrError().flatMap { tag ->
            nfcFacade.readUserBalance(tag)
        }
    }

    private fun shareLogsDialog() {
        SendLogDialogFragment.newInstance(
            sendEmailAddress = getString(R.string.send_email_adress),
            title = getString(R.string.logs_dialog_title),
            message = getString(R.string.logs_dialog_message),
            emailButtonText = getString(R.string.logs_dialog_email_button),
            dialogTheme = R.style.DialogTheme
        ).show(this.supportFragmentManager, "TAG")
        // TODO inside this method in kotlinlogger there is a method getZipOfFiles() that automatically deletes all logs older than 4 days
    }

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    private fun syncState() {
        syncStateDisposable?.dispose()
        syncStateDisposable = synchronizationManager.syncStateObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    SynchronizationState.STARTED -> {
                        activityBinding.appBar.progressBar.visibility = View.VISIBLE
                        activityBinding.appBar.syncButtonArea.visibility = View.INVISIBLE
                    }
                    SynchronizationState.SUCCESS -> {
                        activityBinding.appBar.progressBar.visibility = View.GONE
                        activityBinding.appBar.syncButtonArea.visibility = View.VISIBLE
                        Toast.makeText(
                            this,
                            getString(R.string.data_were_successfully_synchronized),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    SynchronizationState.ERROR -> {
                        activityBinding.appBar.progressBar.visibility = View.GONE
                        activityBinding.appBar.syncButtonArea.visibility = View.VISIBLE
                        if (loginVM.isNetworkConnected().value != true) {
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
                }
            }, {
                Log.e(it)
            })
    }

    private fun checkConnection() {
        connectionDisposable?.dispose()
        connectionDisposable = connectionObserver.getNetworkAvailability()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { available ->
                    loginVM.isNetworkConnected(available)
                },
                {
                }
            )
    }

    override fun getNavView(): NavigationView {
        return activityBinding.navView
    }

    override fun setToolbarVisible(boolean: Boolean) {
        if (boolean) {
            activityBinding.appBar.appBarLayout.visibility = View.VISIBLE
        } else {
            activityBinding.appBar.appBarLayout.visibility = View.GONE
        }
        setDrawerLocked(!boolean)
    }

    override fun setSubtitle(titleText: String?) {
        activityBinding.appBar.toolbar.subtitle = titleText
    }

    override fun setDrawerLocked(boolean: Boolean) {
        if (boolean) {
            activityBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        } else {
            activityBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
    }

    override fun setBackButtonEnabled(boolean: Boolean) {
        getToobarUpButton()?.isEnabled = boolean
        if (!boolean) {
            // I could not find a better method to make the arrow grey when disabled
            getToobarUpButton()?.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.arrow_back))
            getToobarUpButton()?.drawable?.setTint(ContextCompat.getColor(this, R.color.grey))
        } else {
            getToobarUpButton()?.drawable?.setTint(ContextCompat.getColor(this, R.color.black))
        }
    }

    override fun setSyncButtonEnabled(boolean: Boolean) {
        activityBinding.appBar.syncButton.isEnabled = boolean
    }

    override fun loadNavHeader(currentVendorName: String) {
        val metrics: DisplayMetrics = resources.displayMetrics
        navHeaderBinding.ivAppIcon.layoutParams.height = if ((metrics.heightPixels/metrics.density) > 640) {
            resources.getDimensionPixelSize(R.dimen.nav_header_image_height_tall)
        } else {
            resources.getDimensionPixelSize(R.dimen.nav_header_image_height_regular)
        }

        var appVersion = (getString(R.string.app_name) + " " + getString(R.string.version, BuildConfig.VERSION_NAME))
        if (BuildConfig.DEBUG) { appVersion += (" (" + BuildConfig.BUILD_NUMBER + ")") }
        navHeaderBinding.tvAppVersion.text = appVersion

        if(BuildConfig.DEBUG) {
            navHeaderBinding.tvEnvironment.text = getString(
                R.string.environment,
                preferences.url
            )
        } else {
            navHeaderBinding.tvEnvironment.visibility = View.GONE
        }

        if (loginVM.isVendorLoggedIn()) {
            navHeaderBinding.tvUsername.text = currentVendorName
        }
    }

    private fun initPriceUnitSpinner() {
        val currencyAdapter = CurrencyAdapter(this)
        currencyAdapter.init(shopVM.getCurrencies())
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activityBinding.priceUnitSpinner.adapter = currencyAdapter
        shopVM.getCurrency().observe(this, {
            activityBinding.priceUnitSpinner.setSelection(
                currencyAdapter.getPosition(it)
            )
        })
        activityBinding.priceUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                shopVM.setCurrency(activityBinding.priceUnitSpinner.selectedItem as String)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        mainVM.grantPermission(PermissionRequestResult(requestCode, permissions, grantResults))
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //====OnTouchOutsideListener====

    private var mTouchOutsideView: View? = null

    private var mOnTouchOutsideViewListener: OnTouchOutsideViewListener? = null

    /**
     * Sets a listener that is being notified when the user has tapped outside a given view. To remove the listener,
     * call [.removeOnTouchOutsideViewListener].
     *
     *
     * This is useful in scenarios where a view is in edit mode and when the user taps outside the edit mode shall be
     * stopped.
     *
     * @param view
     * @param onTouchOutsideViewListener
     */
    fun setOnTouchOutsideViewListener(
        view: View?,
        onTouchOutsideViewListener: OnTouchOutsideViewListener?
    ) {
        mTouchOutsideView = view
        mOnTouchOutsideViewListener = onTouchOutsideViewListener
    }

    fun getOnTouchOutsideViewListener(): OnTouchOutsideViewListener? {
        return mOnTouchOutsideViewListener
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            // Notify touch outside listener if user tapped outside a given view
            if (mOnTouchOutsideViewListener != null && mTouchOutsideView != null && mTouchOutsideView?.visibility == View.VISIBLE) {
                val viewRect = Rect()
                mTouchOutsideView?.getGlobalVisibleRect(viewRect)
                if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    mOnTouchOutsideViewListener?.onTouchOutside(mTouchOutsideView, ev)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * Interface definition for a callback to be invoked when a touch event has occurred outside a formerly specified
     * view. See [.setOnTouchOutsideViewListener]
     */
    interface OnTouchOutsideViewListener {
        /**
         * Called when a touch event has occurred outside a given view.
         *
         * @param view  The view that has not been touched.
         * @param event The MotionEvent object containing full information about the event.
         */
        fun onTouchOutside(view: View?, event: MotionEvent?)
    }
}
