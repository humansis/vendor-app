package cz.quanti.android.vendor_app.main.checkout.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cz.quanti.android.nfc.dto.v2.UserBalance
import cz.quanti.android.nfc.exception.PINException
import cz.quanti.android.nfc.exception.PINExceptionEnum
import cz.quanti.android.nfc_io_libray.types.NfcUtil
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainViewModel
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.DialogCardPinBinding
import cz.quanti.android.vendor_app.databinding.DialogErrorBinding
import cz.quanti.android.vendor_app.databinding.DialogSuccessBinding
import cz.quanti.android.vendor_app.databinding.FragmentScanCardBinding
import cz.quanti.android.vendor_app.main.checkout.ScanCardAnimation
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel.PaymentStateEnum
import cz.quanti.android.vendor_app.utils.getExpirationDateAsString
import cz.quanti.android.vendor_app.utils.getLimitsAsText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import quanti.com.kotlinlog.Log

class ScanCardFragment : Fragment() {
    private val mainVM: MainViewModel by sharedViewModel()
    private val vm: CheckoutViewModel by sharedViewModel()
    private var paymentDisposable: Disposable? = null
    private var clearCartDisposable: Disposable? = null
    private var displayedDialog: AlertDialog? = null
    private lateinit var activityCallback: ActivityCallback

    private lateinit var scanCardBinding: FragmentScanCardBinding

    private lateinit var scanCardAnimation: ScanCardAnimation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = activity as ActivityCallback
        activityCallback.setSubtitle(null)
        activityCallback.setDrawerLocked(true)
        activityCallback.setSyncButtonEnabled(false)
        scanCardBinding = FragmentScanCardBinding.inflate(inflater, container, false)
        scanCardAnimation = ScanCardAnimation(scanCardBinding.scanCardAnimation)

        return scanCardBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanCardBinding.price.text =
            getString(R.string.total_price, vm.getTotal(), vm.getCurrency())
        init()
    }

    override fun onResume() {
        super.onResume()
        if (arguments?.isEmpty == false) {
            vm.setPin(arguments?.get(PIN_KEY) as String)
            arguments?.clear()
        }
        if (paymentDisposable == null) {
            when {
                vm.getPin() == null -> {
                    showPinDialogAndPayByCard()
                }
                else -> {
                    payByCard()
                }
            }
        }
    }

    override fun onStop() {
        paymentDisposable?.dispose()
        clearCartDisposable?.dispose()
        super.onStop()
    }

    override fun onDestroyView() {
        activityCallback.setDrawerLocked(false)
        activityCallback.setSyncButtonEnabled(true)
        super.onDestroyView()
    }

    private fun init() {
        scanCardBinding.backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked.")
            if (vm.getOriginalCardData().preserveBalance == null) {
                navigateBack()
            } else {
                showPreserveBalanceDialogAndPayByCard()
            }
        }

        vm.getPaymentState().observe(viewLifecycleOwner) {
            val state = it.first
            val result = it.second
            when (state) {
                PaymentStateEnum.READY -> {
                    scanCardAnimation.startScanCardAnimation(result?.throwable != null)
                    scanCardBinding.scanningProgressBar.visibility = View.GONE
                    scanCardBinding.message.text = getString(R.string.scan_card)
                }
                PaymentStateEnum.IN_PROGRESS -> {
                    displayedDialog?.dismiss()
                    scanCardAnimation.stopScanCardAnimation()
                    scanCardBinding.scanningProgressBar.visibility = View.VISIBLE
                    scanCardBinding.message.text = getString(R.string.payment_in_progress)
                }
                PaymentStateEnum.SUCCESS -> {
                    result?.userBalance?.let { balance -> onPaymentSuccessful(balance) }
                }
                PaymentStateEnum.FAILED -> {
                    result?.throwable?.let { throwable -> onPaymentFailed(throwable) }
                }
            }

            updateBackNavigationSettings(state)
        }
    }

    private fun updateBackNavigationSettings(state: PaymentStateEnum) {
        // prevent leaving ScanCardFragment when theres scanning in progress
        val enableLeaving = state != PaymentStateEnum.IN_PROGRESS
        val preservingBalance = vm.getOriginalCardData().preserveBalance != null
        activityCallback.setToolbarUpButtonEnabled(enableLeaving)
        activityCallback.setOnToolbarUpClickListener(
            if (preservingBalance) {
                ::showPreserveBalanceDialogAndPayByCard
            } else {
                ::navigateBack
            }
        )
        scanCardBinding.backButton.isEnabled = (enableLeaving)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // allow to navigate back only in this case
                    if (enableLeaving) {
                        if (preservingBalance) {
                            showPreserveBalanceDialogAndPayByCard()
                        } else {
                            navigateBack()
                        }
                    }
                }
            }
        )
    }

    private fun showPinDialogAndPayByCard() {
        displayedDialog?.dismiss()
        Log.d(TAG, "Showing PIN code dialog")
        val dialogBinding = DialogCardPinBinding.inflate(layoutInflater, null, false)
        dialogBinding.pinTitle.text = getString(R.string.incorrect_pin)
        displayedDialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                Log.d(TAG, "Pin dialog negative button clicked")
                dialog?.dismiss()
                navigateBack()
            }
            .show()
        displayedDialog?.let { dialog ->
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false
            positiveButton.setOnClickListener {
                Log.d(TAG, "Pin dialog positive button clicked")
                val pin = dialogBinding.pinEditText.text.toString()
                when {
                    pin.length == 4 -> {
                        vm.setPin(pin)
                        payByCard()
                        dialog.dismiss()
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

    private fun showPreserveBalanceDialogAndPayByCard() {
        displayedDialog?.dismiss()
        Log.d(TAG, "Showing preserve balance dialog")
        payByCard()
        val dialogBinding = DialogErrorBinding.inflate(layoutInflater, null, false)
        dialogBinding.title.text = getString(R.string.card_error)
        dialogBinding.message.text = getString(R.string.scan_card_to_fix)
        displayedDialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok, null)
            .setOnDismissListener { dialog ->
                Log.d(TAG, "Preserve balance dialog positive button clicked")
                dialog?.dismiss()
            }
            .show()
    }

    private fun payByCard() {
        if (mainVM.enableNfc(requireActivity())) {
            vm.getPin()?.let { pin ->
                paymentDisposable?.dispose()
                paymentDisposable = vm.payByCard(pin)
            }
        }
    }

    private fun onPaymentSuccessful(userBalance: UserBalance) {
        displayedDialog?.dismiss()
        mainVM.successSLE.call()
        val dialogBinding = DialogSuccessBinding.inflate(layoutInflater, null, false)
        dialogBinding.title.text = getString(R.string.success)
        dialogBinding.message.text = getString(
            R.string.card_successfully_paid_new_balance,
            "${userBalance.balance} ${userBalance.currencyCode}" +
                if (userBalance.balance != 0.0) {
                    getExpirationDateAsString(userBalance.expirationDate, requireContext()) +
                        getLimitsAsText(userBalance, requireContext())
                } else {
                    String()
                }
        )
        displayedDialog = AlertDialog.Builder(requireContext(), R.style.SuccessDialogTheme).apply {
            setView(dialogBinding.root)
            setPositiveButton(android.R.string.ok, null)
        }.show()
        vm.setPaymentState(PaymentStateEnum.READY)
        vm.setOriginalCardData(null, null)
        clearCart()
        findNavController().navigate(
            ScanCardFragmentDirections.actionScanCardFragmentToProductsFragment()
        )
    }

    private fun clearCart() {
        clearCartDisposable?.dispose()
        clearCartDisposable = vm.clearCart()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(TAG, "Shopping cart emptied successfully")
            }, {
                Log.e(it)
            })
    }

    private fun onPaymentFailed(throwable: Throwable) {
        mainVM.errorSLE.call()
        vm.setPaymentState(PaymentStateEnum.READY)
        when (throwable) {
            is PINException -> {
                Log.e(TAG, throwable.pinExceptionEnum.name + " TagId: ${NfcUtil.toHexString(throwable.tagId)}")
                when (throwable.pinExceptionEnum) {
                    PINExceptionEnum.INCORRECT_PIN -> {
                        paymentDisposable?.dispose()
                        paymentDisposable = null
                        vm.setPin(null)
                        mainVM.setToastMessage(getNfcCardErrorMessage(throwable.pinExceptionEnum))
                        showPinDialogAndPayByCard()
                    }
                    PINExceptionEnum.PRESERVE_BALANCE -> {
                        Log.d(TAG, "Preserve Balance ${throwable.reconstructPreserveBalance()}.")
                        vm.setOriginalCardData(
                            throwable.reconstructPreserveBalance(),
                            throwable.tagId
                        )
                        mainVM.setToastMessage(getNfcCardErrorMessage(throwable.pinExceptionEnum))
                        showPreserveBalanceDialogAndPayByCard()
                    }
                    PINExceptionEnum.LIMIT_EXCEEDED -> {
                        Log.d(TAG, "Limit exceeded ${throwable.reconstructLimitExceeded()}.")
                        vm.setLimitsExceeded(throwable.reconstructLimitExceeded())
                        navigateBack()
                    }
                    else -> {
                        mainVM.setToastMessage(getNfcCardErrorMessage(throwable.pinExceptionEnum))
                        payByCard()
                    }
                }
            }
            else -> {
                Log.e(this.javaClass.simpleName, throwable)
                mainVM.setToastMessage(getString(R.string.card_error))
                payByCard()
            }
        }
    }

    private fun getNfcCardErrorMessage(error: PINExceptionEnum): String {
        return when (error) {
            PINExceptionEnum.DIFFERENT_CURRENCY -> getString(R.string.card_wrong_currency)
            PINExceptionEnum.DIFFERENT_USER -> getString(R.string.card_wrong_user)
            PINExceptionEnum.INVALID_DATA -> getString(R.string.invalid_data)
            PINExceptionEnum.CARD_LOCKED -> getString(R.string.card_locked)
            PINExceptionEnum.INCORRECT_PIN -> getString(R.string.incorrect_pin)
            PINExceptionEnum.INSUFFICIENT_FUNDS -> getString(R.string.not_enough_money_on_card)
            PINExceptionEnum.TAG_LOST -> getString(R.string.tag_lost_card_error)
            PINExceptionEnum.PRESERVE_BALANCE -> getString(R.string.tag_lost_card_error)
            PINExceptionEnum.BALANCE_EXPIRED -> getString(R.string.card_balance_expired)
            else -> getString(R.string.card_error)
        }
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }

    companion object {
        const val PIN_KEY = "pin"
        private val TAG = ScanCardFragment::class.java.simpleName
    }
}
