package cz.quanti.android.vendor_app.main.checkout.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainViewModel
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.DialogCardPinBinding
import cz.quanti.android.vendor_app.databinding.FragmentCheckoutBinding
import cz.quanti.android.vendor_app.main.checkout.adapter.ScannedVoucherAdapter
import cz.quanti.android.vendor_app.main.checkout.adapter.SelectedProductsAdapter
import cz.quanti.android.vendor_app.main.checkout.callback.CheckoutFragmentCallback
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.constructLimitsExceededMessage
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import quanti.com.kotlinlog.Log

class CheckoutFragment : Fragment(), CheckoutFragmentCallback {
    private val mainVM: MainViewModel by sharedViewModel()
    private val vm: CheckoutViewModel by sharedViewModel()
    private lateinit var selectedProductsAdapter: SelectedProductsAdapter
    private val scannedVoucherAdapter = ScannedVoucherAdapter()
    private var proceedDisposable: Disposable? = null
    private var currencyDisposable: Disposable? = null
    private var updateProductDisposable: Disposable? = null
    private var removeProductDisposable: Disposable? = null
    private var removeProductsDisposable: Disposable? = null
    private var clearCartDisposable: Disposable? = null
    private lateinit var activityCallback: ActivityCallback

    private lateinit var checkoutBinding: FragmentCheckoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = activity as ActivityCallback
        activityCallback.setSubtitle(getString(R.string.checkout))
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    cancel()
                }
            }
        )
        checkoutBinding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return checkoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedProductsAdapter = SelectedProductsAdapter(this, requireContext())
    }

    override fun onStart() {
        super.onStart()

        vm.init()
        initSelectedProductsAdapter()
        initScannedVouchersAdapter()
        initObservers()
        initOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        showIfPurchasePaid()
    }

    override fun onStop() {
        currencyDisposable?.dispose()
        proceedDisposable?.dispose()
        updateProductDisposable?.dispose()
        removeProductDisposable?.dispose()
        removeProductsDisposable?.dispose()
        clearCartDisposable?.dispose()
        super.onStop()
    }

    private fun initSelectedProductsAdapter() {
        val viewManager = LinearLayoutManager(activity)

        checkoutBinding.checkoutSelectedProductsRecyclerView.setHasFixedSize(true)
        checkoutBinding.checkoutSelectedProductsRecyclerView.layoutManager = viewManager
        checkoutBinding.checkoutSelectedProductsRecyclerView.adapter = selectedProductsAdapter
    }

    private fun initScannedVouchersAdapter() {
        val viewManager = LinearLayoutManager(activity)

        checkoutBinding.scannedVouchersRecyclerView.setHasFixedSize(true)
        checkoutBinding.scannedVouchersRecyclerView.layoutManager = viewManager
        checkoutBinding.scannedVouchersRecyclerView.adapter = scannedVoucherAdapter

        scannedVoucherAdapter.setData(vm.getVouchers())
        if (vm.getVouchers().isEmpty()) {
            checkoutBinding.scannedVouchersRecyclerView.visibility = View.INVISIBLE
            checkoutBinding.pleaseScanVoucherTextView.visibility = View.VISIBLE
            checkoutBinding.payByCardButton.visibility = View.VISIBLE
        } else {
            checkoutBinding.scannedVouchersRecyclerView.visibility = View.VISIBLE
            checkoutBinding.pleaseScanVoucherTextView.visibility = View.INVISIBLE
            checkoutBinding.payByCardButton.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservers() {
        currencyDisposable?.dispose()
        currencyDisposable = vm.getCurrencyObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ currency ->
                selectedProductsAdapter.chosenCurrency = currency
                selectedProductsAdapter.notifyDataSetChanged()
                actualizeTotal()
            }, {
                Log.e(TAG, it)
            })

        vm.getSelectedProductsLD().observe(viewLifecycleOwner, { products ->
            selectedProductsAdapter.closeExpandedCard()
            selectedProductsAdapter.setData(products)
            vm.setProducts(products)
            checkForCashbacks(products)
            showIfCartEmpty(products.isNotEmpty())
            actualizeTotal()
        })

        vm.getLimitsExceeded().observe(viewLifecycleOwner, { limitsExceeded ->
            processLimitsExceeded(limitsExceeded)
        })
    }

    private fun initOnClickListeners() {
        checkoutBinding.checkoutFooter.backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked.")
            cancel()
        }

        checkoutBinding.checkoutFooter.clearAllButton.setOnClickListener {
            Log.d(TAG, "Clear All button clicked.")
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle(getString(R.string.are_you_sure_dialog_title))
                .setMessage(getString(R.string.clear_cart_dialog_message))
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    Log.d(TAG, "Positive button clicked.")
                    clearCart()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        checkoutBinding.checkoutFooter.proceedButton.setOnClickListener {
            Log.d(TAG, "Proceed button clicked.")
            proceed()
        }

        checkoutBinding.scanButton.setOnClickListener {
            Log.d(TAG, "Scan button clicked.")
            if (it.isActivated) {
                scanVoucher()
            } else {
                mainVM.setToastMessage(getString(R.string.cashback_with_voucher))
            }
        }

        checkoutBinding.payByCardButton.setOnClickListener {
            Log.d(TAG, "Pay by card button clicked.")
            if (it.isActivated) {
                showPinDialogAndPayByCard()
            } else {
                mainVM.setToastMessage(getString(R.string.only_one_cashback_item_allowed))
            }
        }
    }

    private fun cancel() {
        vm.clearVouchers()
        navigateBack()
    }

    private fun proceed() {
        if (vm.getTotal() <= 0) {
            proceedDisposable?.dispose()
            proceedDisposable = vm.proceed()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    clearCart()
                    vm.clearVouchers()
                    AlertDialog.Builder(requireContext(), R.style.SuccessDialogTheme)
                        .setTitle(getString(R.string.success))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }, {
                    mainVM.setToastMessage(getString(R.string.error_while_proceeding))
                    Log.e(TAG, it)
                })
        } else {
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle(getString(R.string.cannot_proceed_with_purchase_dialog_title))
                .setMessage(getString(R.string.cannot_proceed_with_purchase_dialog_message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun scanVoucher() {
        findNavController().navigate(
            CheckoutFragmentDirections.actionCheckoutFragmentToScannerFragment()
        )
    }

    override fun updateItem(item: SelectedProduct) {
        updateProductDisposable?.dispose()
        updateProductDisposable = vm.updateSelectedProduct(item)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(TAG, "Item $item updated successfully")
            }, {
                Log.e(it)
            })
    }

    override fun removeItemFromCart(product: SelectedProduct) {
        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setTitle(getString(R.string.are_you_sure_dialog_title))
            .setMessage(getString(R.string.remove_product_from_cart_dialog_message))
            .setPositiveButton(
                android.R.string.ok
            ) { _, _ ->
                if (selectedProductsAdapter.itemCount == 1) {
                    clearCart()
                } else {
                    removeProductDisposable?.dispose()
                    removeProductDisposable = vm.removeFromCart(product)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d(TAG, "$product removed successfully")
                        }, {
                            Log.e(it)
                        })
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun showInvalidPriceEnteredMessage() {
        mainVM.setToastMessage(getString(R.string.please_enter_price))
    }

    private fun clearCart() {
        clearCartDisposable?.dispose()
        clearCartDisposable = vm.clearCart()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(TAG, "Shopping cart emptied successfully")
                navigateBack()
            }, {
                Log.e(it)
            })
    }

    private fun showIfCartEmpty(notEmpty: Boolean) {
        if (notEmpty) {
            checkoutBinding.emptyCartTextView.visibility = View.GONE
            checkoutBinding.payByCardButton.isEnabled = mainVM.hasNfcAdapter()
            checkoutBinding.scanButton.isEnabled = true
            checkoutBinding.checkoutFooter.clearAllButton.isEnabled = true
        } else {
            checkoutBinding.emptyCartTextView.visibility = View.VISIBLE
            checkoutBinding.payByCardButton.isEnabled = false
            checkoutBinding.scanButton.isEnabled = false
            checkoutBinding.checkoutFooter.clearAllButton.isEnabled = false
            navigateBack()
        }
    }

    private fun showIfPurchasePaid() {
        if (vm.getVouchers().isNotEmpty()) {
            if (vm.getTotal() <= 0) {
                checkoutBinding.checkoutFooter.proceedButton.visibility = View.VISIBLE
                checkoutBinding.scanButton.isEnabled = false
            } else {
                checkoutBinding.checkoutFooter.proceedButton.visibility = View.GONE
                checkoutBinding.scanButton.isEnabled = true
            }
            checkoutBinding.payByCardButton.visibility = View.INVISIBLE
            checkoutBinding.checkoutFooter.clearAllButton.visibility = View.GONE
        }
    }

    private fun showPinDialogAndPayByCard() {
        if (mainVM.enableNfc(requireActivity())) {
            val dialogBinding = DialogCardPinBinding.inflate(layoutInflater, null, false)
            dialogBinding.pinTitle.text =
                getString(R.string.total_price, vm.getTotal(), vm.getCurrency())
            val dialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setView(dialogBinding.root)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog?.cancel()
                }
                .show()
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false
            positiveButton.setOnClickListener {
                Log.d(TAG, "Dialog positive button clicked")
                val pin = dialogBinding.pinEditText.text.toString()
                when {
                    pin.length == 4 -> {
                        dialog?.dismiss()
                        findNavController().navigate(
                            CheckoutFragmentDirections.actionCheckoutFragmentToScanCardFragment(pin)
                        )
                    }
                    pin.isEmpty() -> {
                        mainVM.setToastMessage(getString(R.string.please_enter_pin))
                    }
                    else -> {
                        mainVM.setToastMessage(getString(R.string.pin_too_short))
                    }
                }
            }

            dialogBinding.pinEditText.doOnTextChanged { text, _, _, _ ->
                positiveButton.isEnabled = !text.isNullOrEmpty()
            }
        }
    }

    private fun processLimitsExceeded(limitsExceeded: Map<Int, Double>) {
        var title = String()
        var message = String()
        var typesToRemove: Set<Int>? = null
        var rightBtnMsg = String()
        when {
            limitsExceeded.size == 1 -> {
                val limitExceeded = limitsExceeded.entries.single()
                val commodityName = CategoryType.getById(limitExceeded.key).stringRes?.let {
                    getString(it)
                }
                title = getString(R.string.limit_exceeded)
                if (limitExceeded.value == 0.0) {
                    message =
                        getString(R.string.commodity_type_not_allowed) + "\n" + commodityName +
                            getString(R.string.remove_commodity_type, commodityName)
                    typesToRemove = setOf(CategoryType.getById(limitExceeded.key).typeId)
                    rightBtnMsg = getString(R.string.cancel)
                } else if (limitExceeded.value > 0) {
                    message = getString(
                        R.string.commodity_type_exceeded,
                        commodityName,
                        String.format("%.2f", limitExceeded.value)
                    ) + "\n\n" + getString(
                        R.string.please_update_cart
                    )
                    rightBtnMsg = getString(android.R.string.ok)
                }
            }
            limitsExceeded.size > 1 -> {
                val exceeded = mutableMapOf<Int, Double>()
                val notAllowed = mutableMapOf<Int, Double>()
                limitsExceeded.forEach {
                    if (it.value == 0.0) {
                        notAllowed[it.key] = it.value
                    } else {
                        exceeded[it.key] = it.value
                    }
                }
                title = getString(R.string.multiple_limits_exceeded)
                message = constructLimitsExceededMessage(exceeded, notAllowed, requireContext())
                typesToRemove = notAllowed.map { CategoryType.getById(it.key).typeId }.toSet()
                rightBtnMsg = getString(android.R.string.ok)
            }
        }
        showLimitsExceededDialog(title, message, typesToRemove, rightBtnMsg)
    }

    private fun showLimitsExceededDialog(
        title: String,
        message: String,
        typesToRemove: Set<Int>? = null,
        rightBtnMsg: String
    ) {
        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                rightBtnMsg
            ) { _, _ ->
                Log.d(TAG, "Positive button clicked.")
            }
            .setNegativeButton(
                getString(R.string.remove_restricted_products),
                null
            )
            .show()
            .apply {
                val negativeButton = this.getButton(AlertDialog.BUTTON_NEGATIVE)
                if (typesToRemove == null) {
                    negativeButton.visibility = View.GONE
                } else {
                    selectedProductsAdapter.setRestrictedTypes(typesToRemove)
                    negativeButton.setOnClickListener {
                        Log.d(TAG, "Negative button clicked.")
                        removeProductsDisposable?.dispose()
                        removeProductsDisposable = vm.removeFromCartByTypes(typesToRemove)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.d(TAG, "Affected products removed successfully")
                            }, {
                                Log.e(it)
                            })
                        this.dismiss()
                    }
                }
            }
    }

    private fun actualizeTotal() {
        val total = vm.getTotal()
        val totalText = "${getString(R.string.total)}:"
        val totalPrice = "${getStringFromDouble(total)} ${vm.getCurrency()}"
        checkoutBinding.totalTextView.text = totalText
        checkoutBinding.totalPriceTextView.text = totalPrice

        if (vm.getVouchers().isNotEmpty()) {
            if (total <= 0) {
                val green = getColor(requireContext(), R.color.green)
                checkoutBinding.totalTextView.setTextColor(green)
                checkoutBinding.totalPriceTextView.setTextColor(green)
            } else {
                val red = getColor(requireContext(), R.color.red)
                checkoutBinding.totalTextView.setTextColor(red)
                checkoutBinding.totalPriceTextView.setTextColor(red)
            }
        }
    }

    private fun checkForCashbacks(products: List<SelectedProduct>) {
        val cashbacks = products.filter { it.product.category.type == CategoryType.CASHBACK }.size
        when {
            cashbacks == 0 -> {
                checkoutBinding.scanButton.isActivated = true
                checkoutBinding.payByCardButton.isActivated = true
            }
            cashbacks == 1 -> {
                checkoutBinding.scanButton.isActivated = false
                checkoutBinding.payByCardButton.isActivated = true
            }
            cashbacks > 1 -> {
                checkoutBinding.scanButton.isActivated = false
                checkoutBinding.payByCardButton.isActivated = false
                mainVM.setToastMessage(getString(R.string.only_one_cashback_item_allowed))
            }
        }
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }

    companion object {
        private val TAG = CheckoutFragment::class.java.simpleName
    }
}
