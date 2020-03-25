package cz.quanti.android.vendor_app.main.vendor.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.ShoppingCartAdapter
import cz.quanti.android.vendor_app.main.vendor.misc.CommonVariables
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.utils.misc.getStringFromDouble
import kotlinx.android.synthetic.main.fragment_shopping_cart.*
import kotlinx.android.synthetic.main.fragment_shopping_cart.shoppingCartFooter
import kotlinx.android.synthetic.main.item_shopping_cart_footer.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShoppingCartFragment() : Fragment() {
    private val vm: VendorViewModel by viewModel()
    private val shoppingCartAdapter = ShoppingCartAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false)
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
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view?.windowToken,
            0
        )

        totalPriceTextView.text =
            getStringFromDouble(getTotalPrice()) + " " + CommonVariables.choosenCurrency
    }

    private fun getTotalPrice(): Double {
        return CommonVariables.cart.map { it.subTotal }.sum()
    }

    private fun initShoppingCartAdapter() {
        val viewManager = LinearLayoutManager(activity)

        shoppingCartRecyclerView.setHasFixedSize(true)
        shoppingCartRecyclerView.layoutManager = viewManager
        shoppingCartRecyclerView.adapter = shoppingCartAdapter

        shoppingCartAdapter.setData(CommonVariables.cart)
    }

    private fun initOnClickListeners() {
        checkoutButton.setOnClickListener {
            findNavController().navigate(VendorFragmentDirections.actionVendorFragmentToCheckoutFragment())
        }

        clearAllButton.setOnClickListener {
            context?.let { context ->
                AlertDialog.Builder(context, R.style.DialogTheme)
                    .setTitle(getString(R.string.areYouSureDialogTitle))
                    .setMessage(getString(R.string.clearCartDialogMessage))
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        clearCart()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            }
        }
    }

    private fun clearCart() {
        CommonVariables.cart.clear()
        shoppingCartAdapter.clearAll()
        noItemsSelectedView.visibility = View.VISIBLE
        shoppingCartFooter.visibility = View.INVISIBLE
    }
}
