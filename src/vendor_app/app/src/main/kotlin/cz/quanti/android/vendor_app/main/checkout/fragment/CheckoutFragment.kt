package cz.quanti.android.vendor_app.main.checkout.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.adapter.SelectedProductsAdapter
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.main.vendor.misc.CommonVariables
import cz.quanti.android.vendor_app.utils.misc.getStringFromDouble
import kotlinx.android.synthetic.main.fragment_checkout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CheckoutFragment : Fragment() {

    private val vm: CheckoutViewModel by viewModel()
    private val selectedProductsAdapter = SelectedProductsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.show()
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClickListeners()
        initSelectedProductsAdapter()

        actualizeTotal()
    }

    private fun initOnClickListeners() {

        cancelButton.setOnClickListener {
            findNavController().navigate(CheckoutFragmentDirections.actionCheckoutFragmentToVendorFragment())
        }

        proceedButton.setOnClickListener {
            if (vm.proceed()) {
                findNavController().navigate(CheckoutFragmentDirections.actionCheckoutFragmentToVendorFragment())
            } else {
                context?.let { context ->
                    AlertDialog.Builder(context, R.style.DialogTheme)
                        .setTitle(getString(R.string.cannotProceedWithPurchaseDialogTitle))
                        .setMessage(getString(R.string.cannotProceedWithPurchaseDialogMessage))
                        .setPositiveButton(android.R.string.yes, null)
                        .show()
                }
            }
        }

        scanButton.setOnClickListener {
            findNavController().navigate(CheckoutFragmentDirections.actionCheckoutFragmentToScannerFragment())
        }
    }

    private fun initSelectedProductsAdapter() {
        val viewManager = LinearLayoutManager(activity)

        checkoutSelectedProductsRecyclerView.setHasFixedSize(true)
        checkoutSelectedProductsRecyclerView.layoutManager = viewManager
        checkoutSelectedProductsRecyclerView.adapter = selectedProductsAdapter

        selectedProductsAdapter.setData(CommonVariables.cart)
    }

    private fun actualizeTotal() {
        val total = vm.getTotal()
        totalTextView.text = getStringFromDouble(total) + " " + CommonVariables.choosenCurrency

        if (total <= 0) {
            context?.let {
                val green = getColor(it, R.color.green)
                moneyIconImageView.imageTintList = ColorStateList.valueOf(green)
                totalTitleTextView.setTextColor(green)
                totalTextView.setTextColor(green)
            }
        } else {
            context?.let {
                val red = getColor(it, R.color.red)
                moneyIconImageView.imageTintList = ColorStateList.valueOf(red)
                totalTitleTextView.setTextColor(red)
                totalTextView.setTextColor(red)
            }
        }
    }
}
