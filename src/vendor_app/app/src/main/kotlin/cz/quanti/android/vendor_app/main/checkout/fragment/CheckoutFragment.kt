package cz.quanti.android.vendor_app.main.checkout.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.adapter.ScannedVoucherAdapter
import cz.quanti.android.vendor_app.main.checkout.adapter.SelectedProductsAdapter
import cz.quanti.android.vendor_app.main.checkout.callback.CheckoutFragmentCallback
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.NfcInitializer
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_card_pin.view.*
import kotlinx.android.synthetic.main.fragment_checkout.*
import kotlinx.android.synthetic.main.item_checkout_footer.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class CheckoutFragment : Fragment(), CheckoutFragmentCallback {
    private val vm: CheckoutViewModel by viewModel()
    private lateinit var selectedProductsAdapter: SelectedProductsAdapter
    private val scannedVoucherAdapter = ScannedVoucherAdapter()
    private var disposable: Disposable? = null
    private var activityCallback: ActivityCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityCallback = activity as ActivityCallback
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    cancel()
                }
            }
        )
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedProductsAdapter = SelectedProductsAdapter(this, requireContext())
    }

    override fun onStart() {
        super.onStart()

        vm.init()
        initObservers()
        initOnClickListeners()
        initSelectedProductsAdapter()
        initScannedVouchersAdapter()
    }

    override fun onResume() {
        super.onResume()

        isPaid()
        isEmptyCart()
        actualizeTotal()
    }

    override fun onStop() {
        disposable?.dispose()
        super.onStop()
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }


    private fun initObservers() {
        vm.getCurrency().observe(viewLifecycleOwner, {
            initSelectedProductsAdapter()
            actualizeTotal()
        })
    }

    private fun initOnClickListeners() {

        backButton?.setOnClickListener {
            cancel()
        }

        clearAllButton.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle(getString(R.string.are_you_sure_dialog_title))
                .setMessage(getString(R.string.clear_cart_dialog_message))
                .setPositiveButton(
                    android.R.string.yes
                ) { _, _ ->
                    clearCart()
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
        }

        proceedButton?.setOnClickListener {
            proceed()
        }

        scanButton?.setOnClickListener {
            scanVoucher()
        }

        payByCardButton?.setOnClickListener {
            payByCard()
        }
    }

    override fun cancel() {
        vm.clearVouchers()
        findNavController().navigate(
            CheckoutFragmentDirections.actionCheckoutFragmentToVendorFragment()
        )
    }

    override fun proceed() {
        if (vm.getTotal() <= 0) {
            disposable?.dispose()
            disposable = vm.proceed().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                    {
                        vm.clearCart()
                        vm.clearVouchers()
                        findNavController().navigate(
                            CheckoutFragmentDirections.actionCheckoutFragmentToVendorFragment()
                        )
                        AlertDialog.Builder(requireContext(), R.style.SuccessDialogTheme)
                            .setTitle(getString(R.string.success))
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    },
                    {
                        Toast.makeText(
                            context,
                            getString(R.string.error_while_proceeding),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(it)
                    }
                )
        } else {
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle(getString(R.string.cannot_proceed_with_purchase_dialog_title))
                .setMessage(getString(R.string.cannot_proceed_with_purchase_dialog_message))
                .setPositiveButton(android.R.string.yes, null)
                .show()
        }
    }

    override fun scanVoucher() {
        findNavController().navigate(
            CheckoutFragmentDirections.actionCheckoutFragmentToScannerFragment()
        )
    }

    override fun payByCard() {
        if (NfcInitializer.initNfc(requireActivity())) {
            showPinDialogAndPayByCard()
        }
    }

    override fun updateItem(position: Int, item: SelectedProduct, newPrice: Double) {
        item.price = newPrice
        vm.updateProduct(position, item)
        actualizeTotal()
        selectedProductsAdapter.notifyDataSetChanged()
    }

    override fun removeItemFromCart(position: Int) {
        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setTitle(getString(R.string.are_you_sure_dialog_title))
            .setMessage(getString(R.string.remove_product_from_cart_dialog_message))
            .setPositiveButton(
                android.R.string.yes
            ) { _, _ ->
                if (selectedProductsAdapter.itemCount == 1) {
                    clearCart()
                } else {
                    vm.removeFromCart(position)
                    selectedProductsAdapter.removeAt(position)
                }
                actualizeTotal()
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    override fun showInvalidPriceEnteredMessage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.please_enter_price),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun clearCart() {
        vm.clearCart()
        selectedProductsAdapter.clearAll()
        isEmptyCart()
        actualizeTotal()
    }

    private fun isEmptyCart() {
        if(vm.getShoppingCart().isNotEmpty()) {
            emptyCartTextView.visibility = View.GONE
            payByCardButton.isEnabled = true
            scanButton.isEnabled = true
            clearAllButton.isEnabled = true
        } else {
            emptyCartTextView.visibility = View.VISIBLE
            payByCardButton.isEnabled = false
            scanButton.isEnabled = false
            clearAllButton.isEnabled = false
        }
    }

    private fun isPaid() {
        if(vm.getVouchers().isNotEmpty()) {
            if (vm.getTotal() <= 0) {
                proceedButton?.visibility = View.VISIBLE
                scanButton.isEnabled = false
            } else {
                proceedButton?.visibility = View.GONE
                scanButton.isEnabled = true
            }
            payByCardButton?.visibility = View.INVISIBLE
            clearAllButton?.visibility = View.GONE
        } else {
            proceedButton?.visibility = View.GONE
            scanButton.isEnabled = true
            payByCardButton?.visibility = View.VISIBLE
            clearAllButton?.visibility = View.VISIBLE
        }
    }

    private fun showPinDialogAndPayByCard() {
       if (NfcInitializer.initNfc(requireActivity())) {
           val dialogView: View = layoutInflater.inflate(R.layout.dialog_card_pin, null)
           dialogView.pin_title.text = getString(R.string.total_price, vm.getTotal(), vm.getCurrency().value)
           AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    val pinEditTextView =
                        dialogView.findViewById<TextInputEditText>(R.id.pinEditText)
                    val pin = pinEditTextView.text.toString()
                    dialog?.dismiss()
                    findNavController().navigate(
                        CheckoutFragmentDirections.actionCheckoutFragmentToScanCardFragment(pin)
                    )
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog?.cancel()
                }
                .show()
        }
    }

    private fun initSelectedProductsAdapter() {
        val viewManager = LinearLayoutManager(activity)

        checkoutSelectedProductsRecyclerView?.setHasFixedSize(true)
        checkoutSelectedProductsRecyclerView?.layoutManager = viewManager
        checkoutSelectedProductsRecyclerView?.adapter = selectedProductsAdapter
        selectedProductsAdapter.chosenCurrency = vm.getCurrency().value.toString()

        selectedProductsAdapter.setData(vm.getShoppingCart())
    }

    private fun initScannedVouchersAdapter() {
        val viewManager = LinearLayoutManager(activity)

        scannedVouchersRecyclerView?.setHasFixedSize(true)
        scannedVouchersRecyclerView?.layoutManager = viewManager
        scannedVouchersRecyclerView?.adapter = scannedVoucherAdapter

        scannedVoucherAdapter.setData(vm.getVouchers())
        if (vm.getVouchers().isEmpty()) {
            scannedVouchersRecyclerView?.visibility = View.INVISIBLE
            pleaseScanVoucherTextView?.visibility = View.VISIBLE
            payByCardButton?.visibility = View.VISIBLE
        } else {
            scannedVouchersRecyclerView?.visibility = View.VISIBLE
            pleaseScanVoucherTextView?.visibility = View.INVISIBLE
            payByCardButton?.visibility = View.INVISIBLE
        }
    }

    private fun actualizeTotal() {
        val total = vm.getTotal()
        val totalText = "${getString(R.string.total)}:"
        val totalPrice = "${getStringFromDouble(total)} ${vm.getCurrency().value}"
        totalTextView?.text = totalText
        totalPriceTextView?.text = totalPrice

        if(vm.getVouchers().isNotEmpty()) {
            if (total <= 0) {
                val green = getColor(requireContext(), R.color.green)
                totalTextView?.setTextColor(green)
                totalPriceTextView?.setTextColor(green)
            } else {
                val red = getColor(requireContext(), R.color.red)
                totalTextView?.setTextColor(red)
                totalPriceTextView?.setTextColor(red)
            }
        }
    }
}
