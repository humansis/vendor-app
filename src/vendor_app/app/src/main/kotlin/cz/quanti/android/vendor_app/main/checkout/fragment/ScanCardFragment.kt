package cz.quanti.android.vendor_app.main.checkout.fragment

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import cz.quanti.android.nfc.exception.PINException
import cz.quanti.android.nfc.exception.PINExceptionEnum
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.utils.VendorAppException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_scan_card.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class ScanCardFragment : Fragment() {
    private val vm: CheckoutViewModel by viewModel()
    private var disposable: Disposable? = null
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.show()
        return inflater.inflate(R.layout.fragment_scan_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireActivity())

        if (nfcAdapter == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_nfc_available),
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigate(
                ScanCardFragmentDirections.actionScanCardFragmentToCheckoutFragment()
            )
        }

        pendingIntent = PendingIntent.getActivity(
            requireActivity(), 0,
            Intent(requireActivity(), requireActivity().javaClass)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )

        init()
        showPinDialogAndPayByCard()
    }

    override fun onResume() {
        super.onResume()

        nfcAdapter?.let { nfcAdapter ->
            if (!nfcAdapter.isEnabled) {
                showWirelessSettings()
            }
            nfcAdapter.enableForegroundDispatch(requireActivity(), pendingIntent, null, null)
        }
    }

    override fun onPause() {
        nfcAdapter?.disableForegroundDispatch(requireActivity())
        super.onPause()
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    private fun init() {
        backButton.setOnClickListener {
            findNavController().navigate(
                ScanCardFragmentDirections.actionScanCardFragmentToCheckoutFragment()
            )
        }
    }

    private fun showPinDialogAndPayByCard() {
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_card_pin, null)
        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val pinEditTextView =
                    dialogView.findViewById<TextInputEditText>(R.id.pinEditText)
                val pin = pinEditTextView.text.toString()
                payByCard(pin)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                findNavController().navigate(
                    ScanCardFragmentDirections.actionScanCardFragmentToCheckoutFragment()
                )
            }
            .show()
    }

    private fun payByCard(pin: String) {
        disposable?.dispose()
        disposable =
            vm.payByCard(pin, vm.getTotal(), vm.getCurrency()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val tag = it.first
                    val balance = it.second.balance

                    AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                        .setTitle(getString(R.string.success))
                        .setMessage(getString(R.string.card_successfuly_paid_new_balance, balance))
                        .setPositiveButton(android.R.string.yes, null)
                        .show()
                    vm.clearShoppingCart()
                    vm.clearCurrency()
                    findNavController().navigate(
                        ScanCardFragmentDirections.actionScanCardFragmentToVendorFragment()
                    )
                }, {
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
                        showPinDialogAndPayByCard()
                    } else {
                        payByCard(pin)
                    }
                })
    }

    private fun showWirelessSettings() {
        Toast.makeText(
            requireContext(),
            getString(R.string.you_need_to_enable_nfc),
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
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
