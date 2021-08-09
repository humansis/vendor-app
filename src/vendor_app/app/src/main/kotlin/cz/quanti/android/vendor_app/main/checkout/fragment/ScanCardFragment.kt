package cz.quanti.android.vendor_app.main.checkout.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        scanCardBinding = FragmentScanCardBinding.inflate(inflater, container, false)
        return scanCardBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    private fun init() {
        scanCardBinding.backButton.setOnClickListener {
            findNavController().navigate(
                ScanCardFragmentDirections.actionScanCardFragmentToCheckoutFragment()
            )
        }
    }

    private fun showPinDialogAndPayByCard() {
        pinDialog?.dismiss()
        val dialogBinding = DialogCardPinBinding.inflate(layoutInflater,null, false)
        val dialogView: View = dialogBinding.root
        dialogBinding.pinTitle.text = getString(R.string.total_price, vm.getTotal(), vm.getCurrency())
        pinDialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setView(dialogView)
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
                         vm.clearCart()
                         vm.clearVouchers()
                         findNavController().navigate(
                             ScanCardFragmentDirections.actionScanCardFragmentToVendorFragment()
                         )
                     }) {
                         when (it) {
                             is PINException -> {
                                 Log.e(this.javaClass.simpleName, it.pinExceptionEnum.name)
                                 Toast.makeText(
                                     requireContext(),
                                     getNfcCardErrorMessage(it.pinExceptionEnum),
                                     Toast.LENGTH_LONG
                                 ).show()
                             }
                             else -> {
                                 Log.e(this.javaClass.simpleName, it)
                                 Toast.makeText(
                                     requireContext(),
                                     getString(R.string.card_error),
                                     Toast.LENGTH_LONG
                                 )
                                     .show()
                             }
                         }

                         if (it is PINException && it.pinExceptionEnum == PINExceptionEnum.INCORRECT_PIN) {
                             paymentDisposable?.dispose()
                             paymentDisposable = null
                             vm.setPin(null)
                             showPinDialogAndPayByCard()
                         } else {
                             payByCard(pin)
                         }
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
            else -> getString(R.string.card_error)
        }
    }
}
