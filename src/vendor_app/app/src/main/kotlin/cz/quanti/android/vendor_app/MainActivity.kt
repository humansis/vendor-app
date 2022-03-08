package cz.quanti.android.vendor_app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.media.MediaPlayer
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import cz.quanti.android.vendor_app.databinding.ActivityMainBinding
import cz.quanti.android.vendor_app.databinding.DialogSyncBinding
import cz.quanti.android.vendor_app.databinding.NavHeaderBinding
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.invoices.viewmodel.InvoicesViewModel
import cz.quanti.android.vendor_app.main.shop.adapter.CurrencyAdapter
import cz.quanti.android.vendor_app.main.shop.viewmodel.ShopViewModel
import cz.quanti.android.vendor_app.main.transactions.viewmodel.TransactionsViewModel
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.login.LoginFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.ConnectionObserver
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import cz.quanti.android.vendor_app.utils.PermissionRequestResult
import cz.quanti.android.vendor_app.utils.SendLogDialogFragment
import cz.quanti.android.vendor_app.utils.getBackgroundColor
import cz.quanti.android.vendor_app.utils.getExpirationDateAsString
import cz.quanti.android.vendor_app.utils.getLimitsAsText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import java.util.Date
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class MainActivity : AppCompatActivity(), ActivityCallback, NfcAdapter.ReaderCallback,
    NavigationView.OnNavigationItemSelectedListener {

    private val loginFacade: LoginFacade by inject()
    private val nfcTagPublisher: NfcTagPublisher by inject()
    private val synchronizationManager: SynchronizationManager by inject()
    private val preferences: AppPreferences by inject()
    private val mainVM: MainViewModel by viewModel()
    private val loginVM: LoginViewModel by viewModel()
    private val shopVM: ShopViewModel by viewModel()
    private val transactionsVM: TransactionsViewModel by viewModel()
    private val invoiceVM: InvoicesViewModel by viewModel()
    private var displayedDialog: AlertDialog? = null
    private var syncDialog: AlertDialog? = null
    private var environmentDisposable: Disposable? = null
    private var connectionDisposable: Disposable? = null
    private var syncStateDisposable: Disposable? = null
    private var syncSubjectDisposable: Disposable? = null
    private var syncDisposable: Disposable? = null
    private var removeProductDisposable: Disposable? = null
    private var emptyDataDisposable: Disposable? = null
    private var readBalanceDisposable: Disposable? = null
    private var selectedProductsDisposable: Disposable? = null
    private var currencyDisposable: Disposable? = null
    private var lastToast: Toast? = null
    private var lastConnectionState: Boolean? = null

    private lateinit var activityBinding: ActivityMainBinding

    private lateinit var connectionObserver: ConnectionObserver

    private lateinit var successPlayer: MediaPlayer
    private lateinit var errorPlayer: MediaPlayer

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        if (!this.resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        activityBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(activityBinding.root)

        connectionObserver = ConnectionObserver(this)
        connectionObserver.registerCallback()

        initNfc()
        setUpToolbar()
        setUpNavigationMenu()
        initObservers()
    }

    override fun onStart() {
        super.onStart()
        setUpBackground()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        logoutIfNotLoggedIn()
        loadNavHeader(loginVM.getCurrentVendorName())
        checkConnection()
        syncState()
        successPlayer = MediaPlayer.create(this, R.raw.end)
        errorPlayer = MediaPlayer.create(this, R.raw.error)
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        synchronizationManager.resetSyncState()
        successPlayer.release()
        errorPlayer.release()
        syncStateDisposable?.dispose()
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        lastToast?.cancel()
        displayedDialog?.dismiss()
        environmentDisposable?.dispose()
        connectionDisposable?.dispose()
        syncStateDisposable?.dispose()
        syncSubjectDisposable?.dispose()
        syncDisposable?.dispose()
        removeProductDisposable?.dispose()
        emptyDataDisposable?.dispose()
        readBalanceDisposable?.dispose()
        selectedProductsDisposable?.dispose()
        currencyDisposable?.dispose()
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        connectionObserver.unregisterCallback()
        super.onDestroy()
    }

    override fun onTagDiscovered(tag: Tag) {
        Log.d(TAG, "onTagDiscovered")
        nfcTagPublisher.getTagSubject().onNext(tag)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onNavigationItemSelected ${item.itemId}")
        when (item.itemId) {
            R.id.shop_button -> {
                findNavController(R.id.nav_host_fragment).navigate(
                    MainNavigationDirections.actionToProductsFragment()
                )
            }
            R.id.transactions_button -> {
                findNavController(R.id.nav_host_fragment).navigate(
                    MainNavigationDirections.actionToTransactionsFragment()
                )
            }
            R.id.invoices_button -> {
                findNavController(R.id.nav_host_fragment).navigate(
                    MainNavigationDirections.actionToInvoicesFragment()
                )
            }
            R.id.read_balance_button -> {
                showReadBalanceDialog()
            }
            R.id.share_logs_button -> {
                shareLogsDialog()
            }
        }
        activityBinding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed")
        if (activityBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            activityBinding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun initNfc() {
        mainVM.initNfcAdapter(this)
        activityBinding.navView.menu.findItem(R.id.read_balance_button).apply {
            val hasNfcAdapter = mainVM.hasNfcAdapter()
            isEnabled = hasNfcAdapter
            isVisible = hasNfcAdapter
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

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        activityBinding.appBar.toolbar.setupWithNavController(
            navHostFragment.navController,
            appBarConfiguration
        )

        synchronizationManager.showDot().observe(this, {
            if (it) {
                activityBinding.appBar.dot.visibility = View.VISIBLE
            } else {
                activityBinding.appBar.dot.visibility = View.INVISIBLE
            }
        })

        mainVM.isNetworkConnected().observe(this, { available ->
            val drawable = if (available) R.drawable.ic_cloud else R.drawable.ic_cloud_offline
            activityBinding.appBar.syncButton.setImageDrawable(
                ContextCompat.getDrawable(this, drawable)
            )
        })

        activityBinding.appBar.syncButton.setOnClickListener {
            Log.d(TAG, "Sync button clicked.")
            if (!logoutIfNotLoggedIn()) {
                synchronizationManager.synchronizeWithServer()
            }
        }
    }

    private fun setUpNavigationMenu() {
        if (!BuildConfig.DEBUG) {
            activityBinding.navView.menu.findItem(R.id.share_logs_button).isVisible = false
        }
        activityBinding.btnLogout.setOnClickListener {
            Log.d(TAG, "Logout button clicked.")
            showLogoutDialog()
            activityBinding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun initObservers() {
        mainVM.successSLE.observe(this, {
            vibrate(this)
            successPlayer.start()
        })
        mainVM.errorSLE.observe(this, {
            vibrate(this)
            errorPlayer.start()
        })

        mainVM.toastMessageSLE.observe(this, { message ->
            message?.let {
                lastToast?.cancel()
                lastToast = Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).apply {
                    show()
                }
            }
        })
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

    private fun showLogoutDialog() {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle(getString(R.string.are_you_sure_dialog_title))
            .setMessage(getString(R.string.logout_dialog_message))
            .setPositiveButton(
                android.R.string.ok
            ) { _, _ ->
                logout()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun logout() {
        emptyData()
        loginFacade.logout()
        findNavController(R.id.nav_host_fragment).popBackStack(R.id.loginFragment, false)
    }

    private fun emptyData() {
        this.cacheDir.deleteRecursively()
        shopVM.setCurrency("")
        emptyDataDisposable?.dispose()
        emptyDataDisposable = shopVM.emptyCart()
            .andThen(shopVM.deleteProducts())
            .andThen(transactionsVM.deleteTransactions())
            .andThen(invoiceVM.deleteInvoices())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(TAG, "Sensitive data deleted successfully")
            }, {
                Log.e(it)
            })
    }

    private fun showReadBalanceDialog() {
        if (mainVM.enableNfc(this)) {
            displayedDialog = AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage(getString(R.string.scan_card))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog?.dismiss()
                    readBalanceDisposable?.dispose()
                    readBalanceDisposable = null
                }
                .create().apply {
                    show()
                }
            showReadBalanceResult()
        }
    }

    private fun showReadBalanceResult() {
        readBalanceDisposable?.dispose()
        readBalanceDisposable = mainVM.readBalance()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                displayedDialog?.dismiss()
                val cardContent = it
                val expirationDate = cardContent.expirationDate
                val cardResultDialog = AlertDialog.Builder(this, R.style.DialogTheme)
                    .setTitle(getString((R.string.read_balance)))
                    .setMessage(
                        if (expirationDate != null && expirationDate < Date()) {
                            getString(R.string.card_balance_expired)
                        } else {
                            getString(
                                R.string.scanning_card_balance,
                                if (cardContent.balance == 0.0) {
                                    "${0.0} ${cardContent.currencyCode}"
                                } else {
                                    "${cardContent.balance} ${cardContent.currencyCode}" +
                                        getExpirationDateAsString(expirationDate, this) +
                                        getLimitsAsText(cardContent, this)
                                }
                            )
                        }
                    )
                    .setCancelable(true)
                    .setNegativeButton(getString(R.string.close)) { dialog, _ ->
                        dialog?.dismiss()
                        readBalanceDisposable?.dispose()
                        readBalanceDisposable = null
                    }
                    .create()
                cardResultDialog.show()
                mainVM.successSLE.call()
                displayedDialog = cardResultDialog
            }, {
                Log.e(TAG, it)
                mainVM.setToastMessage(getString(R.string.card_error))
                mainVM.errorSLE.call()
                displayedDialog?.dismiss()
            })
    }

    private fun shareLogsDialog() {
        SendLogDialogFragment.newInstance(
            sendEmailAddress = getString(R.string.send_email_address),
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
            .subscribe({ state ->
                when (state) {
                    SynchronizationState.STARTED -> {
                        activityBinding.btnLogout.isEnabled = false
                        activityBinding.appBar.progressBar.visibility = View.VISIBLE
                        activityBinding.appBar.syncButtonArea.visibility = View.INVISIBLE
                        syncDialog?.dismiss()
                        syncDialog = showSyncingDialog()
                    }
                    SynchronizationState.SUCCESS -> {
                        activityBinding.btnLogout.isEnabled = true
                        activityBinding.appBar.progressBar.visibility = View.GONE
                        activityBinding.appBar.syncButtonArea.visibility = View.VISIBLE
                        syncDialog?.dismiss()
                        syncDialog = AlertDialog.Builder(this, R.style.DialogTheme)
                            .setTitle(getString(R.string.success))
                            .setMessage(getString(R.string.data_were_successfully_synchronized))
                            .setCancelable(true)
                            .setPositiveButton(getString(android.R.string.ok), null)
                            .show()
                    }
                    SynchronizationState.ERROR -> {
                        activityBinding.btnLogout.isEnabled = true
                        activityBinding.appBar.progressBar.visibility = View.GONE
                        activityBinding.appBar.syncButtonArea.visibility = View.VISIBLE
                        val title = if (mainVM.isNetworkConnected().value != true) {
                            getString(R.string.no_internet_connection)
                        } else {
                            getString(R.string.could_not_synchronize_data_with_server)
                        }
                        val lastSyncError = synchronizationManager.getLastSyncError()
                        val message = if (lastSyncError is CompositeException) {
                            lastSyncError.message + getExceptions(lastSyncError)
                        } else {
                            lastSyncError?.message
                        }
                        syncDialog?.dismiss()
                        syncDialog = AlertDialog.Builder(this, R.style.DialogTheme)
                            .setTitle(title)
                            .setMessage(message)
                            .setCancelable(true)
                            .setPositiveButton(getString(android.R.string.ok), null)
                            .show()
                    }
                    else -> {}
                }
            }, {
                Log.e(TAG, it)
            })
    }

    private fun showSyncingDialog(): AlertDialog {
        val dialogBinding = DialogSyncBinding.inflate(layoutInflater, null, false)
        dialogBinding.title.text = getString(R.string.syncing)
        dialogBinding.progressBar.max = SynchronizationSubject.values().size

        syncSubjectDisposable?.dispose()
        syncSubjectDisposable = synchronizationManager.syncSubjectObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ subject ->
                dialogBinding.message.text = getString(subject.message)
                dialogBinding.progressBar.setProgressCompat(subject.ordinal, true)
            }, {
                Log.e(TAG, it)
            })

        return AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
    }

    private fun getExceptions(lastSyncError: CompositeException): String {
        var string = String()
        lastSyncError.exceptions.forEach {
            string += "\n" + it.message
        }
        return string
    }

    private fun logoutIfNotLoggedIn(): Boolean {
        return if (!loginVM.isVendorLoggedIn()) {
            logout()
            true
        } else if (loginVM.hasInvalidToken(synchronizationManager.getPurchasesCount().blockingFirst())) {
            mainVM.setToastMessage(getString(R.string.token_expired_or_missing))
            logout()
            true
        } else {
            false
        }
    }

    private fun checkConnection() {
        connectionDisposable?.dispose()
        connectionDisposable = connectionObserver.getNetworkAvailability()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { available ->
                    mainVM.isNetworkConnected(available)
                    if (lastConnectionState != available) {
                        mainVM.setToastMessage(
                            getString(
                                if (available)
                                    R.string.connected_to_network
                                else
                                    R.string.network_connection_lost
                            )
                        )
                    }
                    lastConnectionState = available
                },
                {
                    Log.e(TAG, it)
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

    private fun getToolbarUpButton(): ImageButton? {
        val field =
            Class.forName("androidx.appcompat.widget.Toolbar").getDeclaredField("mNavButtonView")
        field.isAccessible = true
        return field.get(activityBinding.appBar.toolbar) as? ImageButton
    }

    override fun setBackButtonEnabled(boolean: Boolean) {
        getToolbarUpButton()?.isEnabled = boolean
        if (!boolean) {
            // I could not find a better method to make the arrow grey when disabled
            getToolbarUpButton()?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.arrow_back
                )
            )
            getToolbarUpButton()?.drawable?.setTint(ContextCompat.getColor(this, R.color.grey))
        } else {
            getToolbarUpButton()?.drawable?.setTint(ContextCompat.getColor(this, R.color.black))
        }
    }

    override fun setSyncButtonEnabled(boolean: Boolean) {
        activityBinding.appBar.syncButton.isEnabled = boolean
    }

    override fun setUpBackground() {
        val color = getBackgroundColor(this, mainVM.getApiHost())
        activityBinding.appBar.toolbar.setBackgroundColor(color)
        activityBinding.appBar.contentMain.navHostFragment.setBackgroundColor(color)
        if (activityBinding.appBar.contentMain.navHostFragment.findNavController().currentDestination?.id != R.id.loginFragment) {
            window.navigationBarColor = color
        }
    }

    override fun loadNavHeader(currentVendorName: String) {
        val navHeaderBinding = NavHeaderBinding.bind(activityBinding.navView.getHeaderView(0))
        val metrics: DisplayMetrics = resources.displayMetrics
        navHeaderBinding.ivAppIcon.layoutParams.height =
            if ((metrics.heightPixels / metrics.density) > 640) {
                resources.getDimensionPixelSize(R.dimen.nav_header_image_height_tall)
            } else {
                resources.getDimensionPixelSize(R.dimen.nav_header_image_height_regular)
            }

        var appVersion = (getString(R.string.app_name) + " " + getString(
            R.string.version,
            BuildConfig.VERSION_NAME
        ))
        if (BuildConfig.DEBUG) {
            appVersion += (" (" + BuildConfig.BUILD_NUMBER + ")")
        }
        navHeaderBinding.tvAppVersion.text = appVersion

        if (BuildConfig.DEBUG) {
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

        initPriceUnitSpinner()
    }

    private fun initPriceUnitSpinner() {
        val currencyAdapter = CurrencyAdapter(this)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activityBinding.priceUnitSpinner.adapter = currencyAdapter

        currencyDisposable?.dispose()
        currencyDisposable = shopVM.getCurrencyObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.isNotBlank()) {
                    if (activityBinding.priceUnitSpinner.adapter.isEmpty) {
                        (activityBinding.priceUnitSpinner.adapter as CurrencyAdapter).init(shopVM.getCurrencies())
                    }
                    activityBinding.priceUnitSpinner.setSelection(
                        currencyAdapter.getPosition(it)
                    )
                }
            }, {
                Log.e(TAG, it)
            })

        activityBinding.priceUnitSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    shopVM.setCurrency(activityBinding.priceUnitSpinner.selectedItem as String)
                    checkForCashbacks(activityBinding.priceUnitSpinner.selectedItem as String)
                }
            }
    }

    private fun checkForCashbacks(currency: String) {
        selectedProductsDisposable?.dispose()
        selectedProductsDisposable = shopVM.getSelectedProducts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ products ->
                products.find {
                    it.product.category.type == CategoryType.CASHBACK &&
                        it.product.currency != currency
                }?.let {
                    removeProductFromCart(it)
                }
            }, {
                Log.e(TAG, it)
            })
    }

    private fun removeProductFromCart(selectedProduct: SelectedProduct) {
        removeProductDisposable?.dispose()
        removeProductDisposable = shopVM.removeSelectedProduct(selectedProduct)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(TAG, "$selectedProduct removed successfully")
                mainVM.setToastMessage(
                    getString(
                        R.string.item_removed_from_cart,
                        selectedProduct.product.name
                    )
                )
            }, {
                Log.e(it)
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        mainVM.grantPermission(PermissionRequestResult(requestCode, permissions, grantResults))
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // ====OnTouchOutsideListener====

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

    @Suppress("unused")
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
         * @param view The view that has not been touched.
         * @param event The MotionEvent object containing full information about the event.
         */
        fun onTouchOutside(view: View?, event: MotionEvent?)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
