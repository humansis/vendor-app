package cz.quanti.android.vendor_app.main.checkout.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cz.quanti.android.nfc.exception.PINException
import cz.quanti.android.nfc.exception.PINExceptionEnum
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.DialogCardPinBinding
import cz.quanti.android.vendor_app.databinding.FragmentScanCardBinding
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.utils.NfcInitializer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class ScanCardFragment : Fragment() {
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
        scanCardBinding.totalPriceText.text = getString(R.string.total_price, vm.getTotal(), vm.getCurrency().value)
        init()
    }

    override fun onResume() {
        super.onResume()
        if(arguments?.isEmpty == false) {
            vm.setPin(arguments?.get("pin").toString())
            arguments?.clear()
        }
        if (paymentDisposable == null) {
            when {
                vm.getPin() == null -> {
                    showPinDialogAndPayByCard()
                }
                else -> vm.getPin()?.let {
                    payByCard(it)
                }
            }
        }
    }

    override fun onPause() {
        NfcInitializer.disableForegroundDispatch(requireActivity())
        super.onPause()
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
            // prevent leaving ScanCardFragment when theres scanning in progress or when card got broken during previous payment
            val enableLeaving = !isInProgress && vm.getOriginalBalance().value == null
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
        val dialogBinding = DialogCardPinBinding.inflate(layoutInflater,null, false)
        dialogBinding.pinTitle.text = getString(R.string.incorrect_pin)
        pinDialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                val pinEditTextView = dialogBinding.pinEditText
                val pin = pinEditTextView.text.toString()
                vm.setPin(pin)
                payByCard(pin)
                dialog?.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog?.dismiss()
                findNavController().navigate(
                    ScanCardFragmentDirections.actionScanCardFragmentToCheckoutFragment()
                )
            }
            .show()

        val positiveButton = pinDialog?.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton?.isEnabled = false

        dialogBinding.pinEditText.doOnTextChanged { text, _, _, _ ->
            positiveButton?.isEnabled = !text.isNullOrEmpty()
        }
    }

     private fun payByCard(pin: String) {
         if (NfcInitializer.initNfc(requireActivity())) {
             paymentDisposable?.dispose()
             paymentDisposable =
                 vm.payByCard(pin, vm.getTotal(), vm.getCurrency().value.toString()).subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe({
                         val balance = it.second.balance
                         AlertDialog.Builder(requireContext(), R.style.SuccessDialogTheme)
                             .setTitle(getString(R.string.success))
                             .setMessage(
                                 String.format(
                                     getString(R.string.card_successfuly_paid_new_balance),
                                     balance
                                 )
                             )
                             .setPositiveButton(android.R.string.ok, null)
                             .show()
                         vm.setScanningInProgress(false)
                         vm.setOriginalBalance(null)
                         vm.setOriginalTagId(null)
                         vm.clearCart()
                         vm.clearVouchers()
                         findNavController().navigate(
                             ScanCardFragmentDirections.actionScanCardFragmentToVendorFragment()
                         )
                     }, {
                         when (it) {
                             is PINException -> {
                                 vm.setOriginalTagId(it.tagId)
                                 Log.e(this.javaClass.simpleName, it.pinExceptionEnum.name)
                                 makeToast(getNfcCardErrorMessage(it.pinExceptionEnum))
                                 when (it.pinExceptionEnum) {
                                     PINExceptionEnum.INCORRECT_PIN -> {
                                         paymentDisposable?.dispose()
                                         paymentDisposable = null
                                         vm.setPin(null)
                                         showPinDialogAndPayByCard()
                                     }
                                     PINExceptionEnum.PRESERVE_BALANCE -> {
                                         it.extraData?.let { originalBalance -> vm.setOriginalBalance(originalBalance.toDouble()) }
                                         scanCardBinding.message.text = getString(R.string.scan_card_to_fix)
                                         scanCardBinding.icon.visibility = View.VISIBLE
                                         payByCard(pin)
                                     }
                                     else -> {
                                         payByCard(pin)
                                     }
                                 }
                             }
                             else -> {
                                 Log.e(this.javaClass.simpleName, it)
                                 makeToast(getString(R.string.card_error))
                                 payByCard(pin)
                             }
                         }
                         vm.setScanningInProgress(false)
                     })
             }
     }

    private fun makeToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
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
}
