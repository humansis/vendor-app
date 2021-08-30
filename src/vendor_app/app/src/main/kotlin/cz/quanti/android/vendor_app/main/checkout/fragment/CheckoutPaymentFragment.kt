package cz.quanti.android.vendor_app.main.checkout.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.adapter.ScannedVoucherAdapter
import cz.quanti.android.vendor_app.main.checkout.callback.CheckoutFragmentCallback
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import kotlinx.android.synthetic.main.fragment_checkout.*
import kotlinx.android.synthetic.main.item_checkout_vouchers_footer.backButton
import kotlinx.android.synthetic.main.item_checkout_vouchers_footer.proceedButton
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class CheckoutPaymentFragment : Fragment() {
    private val vm: CheckoutViewModel by viewModel()

    private val scannedVoucherAdapter = ScannedVoucherAdapter()
    private lateinit var checkoutFragmentCallback: CheckoutFragmentCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.show()
        return inflater.inflate(R.layout.fragment_checkout_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkoutFragmentCallback = parentFragment as CheckoutFragmentCallback
        vm.init()
        initOnClickListeners()
        initScannedVouchersAdapter()
        actualizeTotal()
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
            val green = ContextCompat.getColor(requireContext(), R.color.green)
            moneyIconImageView?.imageTintList = ColorStateList.valueOf(green)
            totalTextView?.setTextColor(green)
        } else {
            val red = ContextCompat.getColor(requireContext(), R.color.red)
            moneyIconImageView?.imageTintList = ColorStateList.valueOf(red)
            totalTextView?.setTextColor(red)
        }
    }

    private fun initOnClickListeners() {
        backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked.")
            checkoutFragmentCallback.cancel()
        }

        proceedButton.setOnClickListener {
            Log.d(TAG, "Proceed button clicked.")
            checkoutFragmentCallback.proceed()
        }

        scanButton?.setOnClickListener {
            Log.d(TAG, "Scan button clicked.")
            checkoutFragmentCallback.scanVoucher()
        }

        payByCardButton?.setOnClickListener {
            Log.d(TAG, "Pay by card button clicked.")
            checkoutFragmentCallback.payByCard()
        }
    }

    companion object {
        private val TAG = CheckoutPaymentFragment::class.java.simpleName
    }
}
