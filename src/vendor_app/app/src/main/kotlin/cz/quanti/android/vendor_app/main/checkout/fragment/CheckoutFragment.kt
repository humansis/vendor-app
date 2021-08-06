package cz.quanti.android.vendor_app.main.checkout.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.DialogCardPinBinding
import cz.quanti.android.vendor_app.databinding.FragmentCheckoutBinding
import cz.quanti.android.vendor_app.databinding.ItemCheckoutFooterBinding
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class CheckoutFragment : Fragment(), CheckoutFragmentCallback {
    private val vm: CheckoutViewModel by viewModel()
    private lateinit var selectedProductsAdapter: SelectedProductsAdapter
    private val scannedVoucherAdapter = ScannedVoucherAdapter()
    private var disposable: Disposable? = null
    private lateinit var activityCallback: ActivityCallback

    private lateinit var checkoutBinding: FragmentCheckoutBinding
    private lateinit var footerBinding: ItemCheckoutFooterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = activity as ActivityCallback
        activityCallback.setToolbarVisible(true)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    cancel()
                }
            }
        )
        checkoutBinding = FragmentCheckoutBinding.inflate(inflater, container, false)
        footerBinding = ItemCheckoutFooterBinding.inflate(inflater, container, false)
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
        disposable?.dispose()
        super.onStop()
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
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
        vm.getCurrency().observe(viewLifecycleOwner, {
            selectedProductsAdapter.chosenCurrency = vm.getCurrency().value.toString()
            selectedProductsAdapter.notifyDataSetChanged()
            actualizeTotal()
        })

        vm.getSelectedProducts().observe(viewLifecycleOwner, {
            selectedProductsAdapter.closeExpandedCard()
            selectedProductsAdapter.setData(it)
            vm.setProducts(it)
            showIfCartEmpty(it.isNotEmpty())
            actualizeTotal()
        })
    }

    private fun initOnClickListeners() {

        footerBinding.backButton.setOnClickListener {
            cancel()
        }

        footerBinding.clearAllButton.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle(getString(R.string.are_you_sure_dialog_title))
                .setMessage(getString(R.string.clear_cart_dialog_message))
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    clearCart()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        footerBinding.proceedButton.setOnClickListener {
            proceed()
        }

        checkoutBinding.scanButton.setOnClickListener {
            scanVoucher()
        }

        checkoutBinding.payByCardButton.setOnClickListener {
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
                .setPositiveButton(android.R.string.ok, null)
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

    override fun updateItem(item: SelectedProduct, newPrice: Double) {
        item.price = newPrice
        vm.updateSelectedProduct(item)
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
                    vm.removeFromCart(product)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
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
    }

    private fun showIfCartEmpty(notEmpty: Boolean) {
        if(notEmpty) {
            checkoutBinding.emptyCartTextView.visibility = View.GONE
            checkoutBinding.payByCardButton.isEnabled = true
            checkoutBinding.scanButton.isEnabled = true
            footerBinding.clearAllButton.isEnabled = true
        } else {
            checkoutBinding.emptyCartTextView.visibility = View.VISIBLE
            checkoutBinding.payByCardButton.isEnabled = false
            checkoutBinding.scanButton.isEnabled = false
            footerBinding.clearAllButton.isEnabled = false
        }
    }

    private fun showIfPurchasePaid() {
        if(vm.getVouchers().isNotEmpty()) {
            if (vm.getTotal() <= 0) {
                footerBinding.proceedButton.visibility = View.VISIBLE
                checkoutBinding.scanButton.isEnabled = false
            } else {
                footerBinding.proceedButton.visibility = View.GONE
                checkoutBinding.scanButton.isEnabled = true
            }
            checkoutBinding.payByCardButton.visibility = View.INVISIBLE
            footerBinding.clearAllButton.visibility = View.GONE
        } else {
            footerBinding.proceedButton.visibility = View.GONE
            checkoutBinding.scanButton.isEnabled = true
            checkoutBinding.payByCardButton.visibility = View.VISIBLE
            footerBinding.clearAllButton.visibility = View.VISIBLE
        }
    }

    private fun showPinDialogAndPayByCard() {
       if (NfcInitializer.initNfc(requireActivity())) {
           val dialogBinding = DialogCardPinBinding.inflate(layoutInflater,null, false)
           val dialogView: View = dialogBinding.root
           dialogBinding.pinTitle.text = getString(R.string.total_price, vm.getTotal(), vm.getCurrency().value)
           AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    val pinEditTextView = dialogBinding.pinEditText
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

    private fun actualizeTotal() {
        val total = vm.getTotal()
        val totalText = "${getString(R.string.total)}:"
        val totalPrice = "${getStringFromDouble(total)} ${vm.getCurrency().value}"
        checkoutBinding.totalTextView.text = totalText
        checkoutBinding.totalPriceTextView.text = totalPrice

        if(vm.getVouchers().isNotEmpty()) {
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
}
