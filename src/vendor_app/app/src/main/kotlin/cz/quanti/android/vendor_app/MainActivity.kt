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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.toLiveData
import androidx.navigation.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc.dto.UserBalance
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.shop.adapter.CurrencyAdapter
import cz.quanti.android.vendor_app.main.shop.viewmodel.ShopViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import cz.quanti.android.vendor_app.utils.NfcInitializer
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import extensions.isNetworkConnected
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log
import quanti.com.kotlinlog.file.SendLogDialogFragment


class MainActivity : AppCompatActivity(), ActivityCallback,
    NavigationView.OnNavigationItemSelectedListener {

    private val loginFacade: LoginFacade by inject()
    private val syncFacade: SynchronizationFacade by inject()
    private val preferences: AppPreferences by inject()
    private val nfcTagPublisher: NfcTagPublisher by inject()
    private val nfcFacade: VendorFacade by inject()
    private val synchronizationManager: SynchronizationManager by inject()
    private var nfcAdapter: NfcAdapter? = null
    private val loginVM: LoginViewModel by viewModel()
    private val vm: ShopViewModel by viewModel()
    private var displayedDialog: AlertDialog? = null
    private var disposable: Disposable? = null
    private var syncStateDisposable: Disposable? = null
    private var syncDisposable: Disposable? = null
    private var readBalanceDisposable: Disposable? = null
    private lateinit var drawer: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var appBar: AppBarLayout

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!this.resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        appBar = findViewById(R.id.appBarLayout)
        drawer = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        setUpToolbar()
        initPriceUnitSpinner()
        btn_logout.setOnClickListener {
            logout()
            drawer.closeDrawer(GravityCompat.START)
        }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        loadNavHeader(loginVM.getCurrentVendorName())
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_button -> {
                findNavController(R.id.nav_host_fragment).popBackStack(R.id.productFragment, false)
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
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setUpToolbar() {
        syncFacade.getPurchasesCountLD()
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
            .observe(this, { purchasesCount ->
                disposable?.dispose()
                disposable = syncFacade.isSyncNeeded(purchasesCount)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            showDot(it)
                        },
                        {
                        }
                    )
            })

        syncButton?.setOnClickListener {
            synchronizationManager.synchronizeWithServer()
        }
    }

    override fun setTitle(titleText: String) {
        appbar_title.text = titleText
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
                android.R.string.yes
            ) { _, _ ->
                loginFacade.logout()
                findNavController(R.id.nav_host_fragment).popBackStack(R.id.loginFragment, false)
            }
            .setNegativeButton(android.R.string.no, null)
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
                        progressBar?.visibility = View.VISIBLE
                        syncButtonArea?.visibility = View.INVISIBLE
                    }
                    SynchronizationState.SUCCESS -> {
                        progressBar?.visibility = View.GONE
                        syncButtonArea?.visibility = View.VISIBLE
                        Toast.makeText(
                            this,
                            getString(R.string.data_were_successfully_synchronized),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    SynchronizationState.ERROR -> {
                        progressBar?.visibility = View.GONE
                        syncButtonArea?.visibility = View.VISIBLE
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
                }
            }, {
                Log.e(it)
            })
    }

    override fun showDot(boolean: Boolean) {
        if (boolean) {
            dot?.visibility = View.VISIBLE
        } else {
            dot?.visibility = View.INVISIBLE
        }
    }

    override fun setToolbarVisible(boolean: Boolean) {
        if (boolean) {
            appBar.visibility = View.VISIBLE
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        } else {
            appBar.visibility = View.GONE
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    override fun loadNavHeader(currentVendorName: String) {
        val metrics: DisplayMetrics = resources.displayMetrics
        val ivAppIcon = nav_view.getHeaderView(0).findViewById<ImageView>(R.id.iv_app_icon)
        ivAppIcon.layoutParams.height = if ((metrics.heightPixels/metrics.density) > 640) {
            resources.getDimensionPixelSize(R.dimen.nav_header_image_height_tall)
        } else {
            resources.getDimensionPixelSize(R.dimen.nav_header_image_height_regular)
        }

        val tvAppVersion = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tv_app_version)
        var appVersion = (getString(R.string.app_name) + " " + getString(R.string.version, BuildConfig.VERSION_NAME))
        if (BuildConfig.DEBUG) { appVersion += (" (" + BuildConfig.BUILD_NUMBER + ")") }
        tvAppVersion.text = appVersion

        val tvEnvironment = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tv_environment)
        if(BuildConfig.DEBUG) {
            tvEnvironment.text = getString(
                R.string.environment,
                preferences.url
            )
        } else {
            tvEnvironment.visibility = View.GONE
        }

        if (loginVM.isVendorLoggedIn()) {
            val tvUsername = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tv_username)
            tvUsername.text = currentVendorName
        }
    }

    private fun initPriceUnitSpinner() {
        val currencyAdapter = CurrencyAdapter(this)
        currencyAdapter.init(vm.getCurrencies())
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        priceUnitSpinner.adapter = currencyAdapter
        vm.getCurrency().observe(this, {
            priceUnitSpinner.setSelection(
                currencyAdapter.getPosition(it)
            )
        })
        priceUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                vm.setCurrency(priceUnitSpinner.selectedItem as String)
            }
        }
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
            if (mOnTouchOutsideViewListener != null && mTouchOutsideView != null && mTouchOutsideView!!.visibility == View.VISIBLE) {
                val viewRect = Rect()
                mTouchOutsideView!!.getGlobalVisibleRect(viewRect)
                if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    mOnTouchOutsideViewListener!!.onTouchOutside(mTouchOutsideView, ev)
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
