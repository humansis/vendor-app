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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.adapter.SelectedProductsAdapter
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_checkout.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class CheckoutFragment() : Fragment() {
    private val vm: CheckoutViewModel by viewModel()
    private val selectedProductsAdapter = SelectedProductsAdapter(this)

    private val args: CheckoutFragmentArgs by navArgs()

    lateinit var chosenCurrency: String
    lateinit var cart: MutableList<SelectedProduct>
    lateinit var vouchers: MutableList<Voucher>

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

        chosenCurrency = args.currency
        cart = (activity as MainActivity).cart
        vouchers = (activity as MainActivity).vouchers

        vm.init(chosenCurrency, cart, vouchers)
        initOnClickListeners()
        initSelectedProductsAdapter()

        actualizeTotal()
    }

    private fun initOnClickListeners() {

        cancelButton.setOnClickListener {
            findNavController().navigate(
                CheckoutFragmentDirections.actionCheckoutFragmentToVendorFragment(
                    chosenCurrency
                )
            )
        }

        proceedButton.setOnClickListener {
            if (vm.getTotal() <= 0) {
                vm.proceed(vouchers).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(
                        {
                            cart.clear()
                            vouchers.clear()
                            findNavController().navigate(
                                CheckoutFragmentDirections.actionCheckoutFragmentToVendorFragment("")
                            )
                        },
                        {
                            // TODO dialog
                            Log.e(it)
                        }
                )
            } else {
                AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                    .setTitle(getString(R.string.cannotProceedWithPurchaseDialogTitle))
                    .setMessage(getString(R.string.cannotProceedWithPurchaseDialogMessage))
                    .setPositiveButton(android.R.string.yes, null)
                    .show()
            }
        }

        scanButton.setOnClickListener {
            findNavController().navigate(
                CheckoutFragmentDirections.actionCheckoutFragmentToScannerFragment(
                    chosenCurrency
                )
            )
        }
    }

    private fun initSelectedProductsAdapter() {
        val viewManager = LinearLayoutManager(activity)

        checkoutSelectedProductsRecyclerView.setHasFixedSize(true)
        checkoutSelectedProductsRecyclerView.layoutManager = viewManager
        checkoutSelectedProductsRecyclerView.adapter = selectedProductsAdapter

        selectedProductsAdapter.setData(cart)
    }

    private fun actualizeTotal() {
        val total = vm.getTotal()
        val totalText = "${getStringFromDouble(total)} $chosenCurrency"
        totalTextView.text = totalText

        if (total <= 0) {
            val green = getColor(requireContext(), R.color.green)
            moneyIconImageView.imageTintList = ColorStateList.valueOf(green)
            totalTitleTextView.setTextColor(green)
            totalTextView.setTextColor(green)
        } else {
            val red = getColor(requireContext(), R.color.red)
            moneyIconImageView.imageTintList = ColorStateList.valueOf(red)
            totalTitleTextView.setTextColor(red)
            totalTextView.setTextColor(red)
        }
    }
}
