package cz.quanti.android.vendor_app.main.checkout.fragment

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.CheckoutScreenState
import cz.quanti.android.vendor_app.main.checkout.adapter.ScannedVoucherAdapter
import cz.quanti.android.vendor_app.main.checkout.adapter.SelectedProductsAdapter
import cz.quanti.android.vendor_app.main.checkout.callback.CheckoutFragmentCallback
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_checkout.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class CheckoutFragment() : Fragment(), CheckoutFragmentCallback {
    private val vm: CheckoutViewModel by viewModel()
    private val selectedProductsAdapter = SelectedProductsAdapter()
    private val scannedVoucherAdapter = ScannedVoucherAdapter()
    private var state = CheckoutScreenState.STATE_PAYMENT_SHOWED
    private var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.show()
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        state = vm.getScreenState()

        val fragment = getFragmentFromState()

        if (!isLandscapeOriented()) {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.checkoutFragmentContainer, fragment)
            }
            transaction.commit()
        }

        vm.init()
        initOnClickListeners()
        initSelectedProductsAdapter()
        initScannedVouchersAdapter()

        actualizeTotal()
    }

    override fun onStop() {
        disposable?.dispose()
        super.onStop()
    }

    override fun onDestroy() {
        vm.setScreenState(state)
        disposable?.dispose()
        super.onDestroy()
    }

    override fun showCart() {
        state = CheckoutScreenState.STATE_PRODUCTS_SHOWED

        val transaction = childFragmentManager.beginTransaction().apply {
            replace(R.id.checkoutFragmentContainer, CheckoutProductsFragment())
        }
        transaction.commit()
    }

    override fun goToPayment() {
        state = CheckoutScreenState.STATE_PAYMENT_SHOWED

        val transaction = childFragmentManager.beginTransaction().apply {
            replace(R.id.checkoutFragmentContainer, CheckoutPaymentFragment())
        }
        transaction.commit()
    }

    private fun getFragmentFromState(): Fragment {
        return when (state) {
            CheckoutScreenState.STATE_PAYMENT_SHOWED -> {
                CheckoutPaymentFragment()
            }
            CheckoutScreenState.STATE_PRODUCTS_SHOWED -> {
                CheckoutProductsFragment()
            }
        }
    }

    private fun initOnClickListeners() {

        cancelButton?.setOnClickListener {
            cancel()
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
                        vm.clearShoppingCart()
                        vm.clearVouchers()
                        vm.clearCurrency()
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
        findNavController().navigate(
            CheckoutFragmentDirections.actionCheckoutFragmentToScanCardFragment()
        )
    }

    private fun initSelectedProductsAdapter() {
        val viewManager = LinearLayoutManager(activity)

        checkoutSelectedProductsRecyclerView?.setHasFixedSize(true)
        checkoutSelectedProductsRecyclerView?.layoutManager = viewManager
        checkoutSelectedProductsRecyclerView?.adapter = selectedProductsAdapter
        selectedProductsAdapter.chosenCurrency = vm.getCurrency()

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
        val totalText = "${getStringFromDouble(total)} ${vm.getCurrency()}"
        totalTextView?.text = totalText

        if (total <= 0) {
            val green = getColor(requireContext(), R.color.green)
            moneyIconImageView?.imageTintList = ColorStateList.valueOf(green)
            totalTitleTextView?.setTextColor(green)
            totalTextView?.setTextColor(green)
        } else {
            val red = getColor(requireContext(), R.color.red)
            moneyIconImageView?.imageTintList = ColorStateList.valueOf(red)
            totalTitleTextView?.setTextColor(red)
            totalTextView?.setTextColor(red)
        }
    }

    private fun isLandscapeOriented(): Boolean {
        return requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}
