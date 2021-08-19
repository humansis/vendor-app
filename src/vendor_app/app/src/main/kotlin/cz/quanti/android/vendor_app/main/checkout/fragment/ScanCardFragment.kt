package cz.quanti.android.vendor_app.main.checkout.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cz.quanti.android.nfc.dto.UserBalance
import cz.quanti.android.nfc.exception.PINException
import cz.quanti.android.nfc.exception.PINExceptionEnum
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainViewModel
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.DialogCardPinBinding
import cz.quanti.android.vendor_app.databinding.DialogSuccessBinding
import cz.quanti.android.vendor_app.databinding.FragmentScanCardBinding
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class ScanCardFragment : Fragment() {
    private val mainVM: MainViewModel by sharedViewModel()
    private val vm: CheckoutViewModel by viewModel()
    private var paymentDisposable: Disposable? = null
    private var pinDialog: AlertDialog? = null
    private lateinit var activityCallback: ActivityCallback

    private lateinit var scanCardBinding: FragmentScanCardBinding

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

        return scanCardBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanCardBinding.price.text  = getString(R.string.total_price, vm.getTotal(), vm.getCurrency().value)
        init()
    }

    override fun onResume() {
        super.onResume()
        if (arguments?.isEmpty == false) {
            vm.setPin(arguments?.get(PIN_KEY).toString())
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
        paymentDisposable = null
        super.onStop()
    }

    override fun onDestroyView() {
        activityCallback.setDrawerLocked(false)
        activityCallback.setSyncButtonEnabled(true)
        super.onDestroyView()
    }

    private fun init() {
        scanCardBinding.backButton.setOnClickListener {
            findNavController().navigate(
                ScanCardFragmentDirections.actionScanCardFragmentToCheckoutFragment()
            )
        }

        vm.getScanningInProgress().observe(viewLifecycleOwner, { isInProgress ->
            // show spinning progressbar if scanning is in progress
            if (isInProgress) {
                scanCardBinding.price.visibility = View.GONE
                scanCardBinding.icon.visibility = View.GONE
                scanCardBinding.scanningProgressBar.visibility = View.VISIBLE
                scanCardBinding.message.text = getString(R.string.payment_in_progress)
            } else {
                scanCardBinding.scanningProgressBar.visibility = View.GONE
                if (vm.getOriginalCardData().value?.balance == null) {
                    scanCardBinding.price.visibility = View.VISIBLE
                    scanCardBinding.message.text = getString(R.string.scan_card)
                } else {
                    scanCardBinding.icon.visibility = View.VISIBLE
                    scanCardBinding.message.text = getString(R.string.scan_card_to_fix)
                }
            }

            // prevent leaving ScanCardFragment when theres scanning in progress or when card got broken during previous payment
            val enableLeaving = !isInProgress && vm.getOriginalCardData().value?.balance == null
            activityCallback.setBackButtonEnabled(enableLeaving)
            scanCardBinding.backButton.isEnabled = (enableLeaving)
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (enableLeaving) {
                            // allow to navigate back only in this case
                            requireActivity().onBackPressed()
                        }
                    }
                }
            )
        })
    }

    private fun showPinDialogAndPayByCard() {
        pinDialog?.dismiss()
        val dialogBinding = DialogCardPinBinding.inflate(layoutInflater, null, false)
        dialogBinding.pinTitle.text = getString(R.string.incorrect_pin)
        pinDialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog?.dismiss()
                findNavController().navigate(
                    ScanCardFragmentDirections.actionScanCardFragmentToCheckoutFragment()
                )
            }
            .show()
        pinDialog?.let { dialog ->
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false
            positiveButton.setOnClickListener {
                val pin = dialogBinding.pinEditText.text.toString()
                if (pin.isEmpty()) {
                    mainVM.setToastMessage(getString(R.string.please_enter_pin))
                } else {
                    dialog.dismiss()
                    findNavController().navigate(
                        CheckoutFragmentDirections.actionCheckoutFragmentToScanCardFragment(pin)
                    )
                }
            }

            dialogBinding.pinEditText.doOnTextChanged { text, _, _, _ ->
                positiveButton.isEnabled = !text.isNullOrEmpty()
            }
        }
    }

     private fun payByCard() {
         if (mainVM.enableNfc(requireActivity())) {
             paymentDisposable?.dispose()
             paymentDisposable =
                 vm.getPin()?.let { pin ->
                     vm.payByCard(pin, vm.getTotal(), vm.getCurrency().value.toString()).subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribe({
                             onPaymentSuccessful(it)
                         }, {
                             onPaymentFailed(it)
                         })
                 }
         }
     }

    private fun onPaymentSuccessful(userBalance: UserBalance) {
        mainVM.successSLE.call()
        val dialogBinding = DialogSuccessBinding.inflate(layoutInflater,null, false)
        dialogBinding.title.text = getString(R.string.success)
        dialogBinding.message.text = String.format(
            getString(R.string.card_successfuly_paid_new_balance),
            userBalance.balance,
            userBalance.currencyCode
        )
        AlertDialog.Builder(requireContext(), R.style.SuccessDialogTheme).apply {
            setView(dialogBinding.root)
            setPositiveButton(android.R.string.ok, null)
        }.show()
        vm.setScanningInProgress(false)
        vm.setOriginalCardData(null, null)
        vm.clearCart()
        vm.clearVouchers()
        findNavController().navigate(
            ScanCardFragmentDirections.actionScanCardFragmentToVendorFragment()
        )
    }

    private fun onPaymentFailed(throwable: Throwable) {
        mainVM.errorSLE.call()
        vm.setScanningInProgress(false)
        when (throwable) {
            is PINException -> {
                Log.e(this.javaClass.simpleName, throwable.pinExceptionEnum.name)
                mainVM.setToastMessage(getNfcCardErrorMessage(throwable.pinExceptionEnum))
                when (throwable.pinExceptionEnum) {
                    PINExceptionEnum.INCORRECT_PIN -> {
                        paymentDisposable?.dispose()
                        paymentDisposable = null
                        vm.setPin(null)
                        showPinDialogAndPayByCard()
                    }
                    PINExceptionEnum.PRESERVE_BALANCE -> {
                        throwable.extraData?.let { originalBalance ->
                            vm.setOriginalCardData(originalBalance.toDouble(), throwable.tagId)
                        }
                        payByCard()
                    }
                    else -> {
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
            PINExceptionEnum.INVALID_DATA -> getString(R.string.invalid_data)
            PINExceptionEnum.CARD_LOCKED -> getString(R.string.card_locked)
            PINExceptionEnum.INCORRECT_PIN -> getString(R.string.incorrect_pin)
            PINExceptionEnum.INSUFFICIENT_FUNDS -> getString(R.string.not_enough_money_on_card)
            PINExceptionEnum.TAG_LOST -> getString(R.string.tag_lost_card_error)
            PINExceptionEnum.PRESERVE_BALANCE -> getString(R.string.tag_lost_card_error)
            else -> getString(R.string.card_error)
        }
    }

    companion object {
        const val PIN_KEY = "pin"
    }
}
