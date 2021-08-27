package cz.quanti.android.vendor_app.main.vendor.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.app.AlertDialog
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.ShoppingCartAdapter
import cz.quanti.android.vendor_app.main.vendor.callback.ShoppingCartFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import cz.quanti.android.vendor_app.utils.round
import kotlinx.android.synthetic.main.fragment_shopping_cart.*
import kotlinx.android.synthetic.main.fragment_shopping_cart.shoppingCartFooter
import kotlinx.android.synthetic.main.item_shopping_cart_footer.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class ShoppingCartFragment : Fragment(), ShoppingCartFragmentCallback {
    private val vm: VendorViewModel by viewModel()
    private lateinit var shoppingCartAdapter: ShoppingCartAdapter
    private lateinit var vendorFragmentCallback: VendorFragmentCallback
    lateinit var chosenCurrency: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vendorFragmentCallback = parentFragment as VendorFragmentCallback
        chosenCurrency = vm.getCurrency()
        shoppingCartAdapter = ShoppingCartAdapter(this)
    }

    override fun onStart() {
        super.onStart()
        initShoppingCartAdapter()
        initOnClickListeners()

        if (shoppingCartAdapter.itemCount == 0) {
            noItemsSelectedView.visibility = View.VISIBLE
            shoppingCartFooter.visibility = View.INVISIBLE
        } else {
            noItemsSelectedView.visibility = View.INVISIBLE
            shoppingCartFooter.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view?.windowToken,
            0
        )

        updateTotal()
    }

    override fun removeItemFromCart(position: Int) {
        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setTitle(getString(R.string.are_you_sure_dialog_title))
            .setMessage(getString(R.string.remove_product_from_cart_dialog_message))
            .setPositiveButton(
                android.R.string.yes
            ) { _, _ ->
                if (shoppingCartAdapter.itemCount == 1) {
                    clearCart()
                } else {
                    vm.removeFromCart(position)
                    shoppingCartAdapter.removeAt(position)
                    updateTotal()
                }
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    override fun getCurrency(): String {
        return chosenCurrency
    }

    private fun getTotalPrice(): Double {
        return round(vm.getShoppingCart().map { it.price }.sum(),3)
    }

    private fun initShoppingCartAdapter() {
        val viewManager = LinearLayoutManager(activity)

        shoppingCartRecyclerView.setHasFixedSize(true)
        shoppingCartRecyclerView.layoutManager = viewManager
        shoppingCartRecyclerView.adapter = shoppingCartAdapter

        shoppingCartAdapter.setData(vm.getShoppingCart())
    }

    private fun initOnClickListeners() {
        checkoutButton.setOnClickListener {
            Log.d("Checkout button clicked.")
            if (shoppingCartAdapter.itemCount > 0){
                findNavController().navigate(
                    VendorFragmentDirections.actionVendorFragmentToCheckoutFragment()
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.empty_cart),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        clearAllButton.setOnClickListener {
            Log.d("Clear all button clicked.")
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
    }

    private fun clearCart() {
        vm.clearCart()
        shoppingCartAdapter.clearAll()
        noItemsSelectedView.visibility = View.VISIBLE
        shoppingCartFooter.visibility = View.INVISIBLE
    }

    private fun updateTotal() {
        val totalText = "${getString(R.string.total)}: ${getStringFromDouble(getTotalPrice())} ${chosenCurrency}"
        totalPriceTextView.text = totalText
    }
}
