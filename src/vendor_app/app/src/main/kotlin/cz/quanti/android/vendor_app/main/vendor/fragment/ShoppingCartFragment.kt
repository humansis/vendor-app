package cz.quanti.android.vendor_app.main.vendor.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.misc.CommonVariables
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import kotlinx.android.synthetic.main.fragment_shopping_cart.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShoppingCartFragment(): Fragment() {
    private val vm: VendorViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false)
    }

    override fun onStart() {
        super.onStart()
        initShoppingCartAdapter()

        if (CommonVariables.shoppingCartAdapter.itemCount == 0) {
            noItemsSelectedView.visibility = View.VISIBLE
        } else {
            noItemsSelectedView.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun initShoppingCartAdapter() {
        val viewManager = LinearLayoutManager(activity)

        shoppingCartRecyclerView.setHasFixedSize(true)
        shoppingCartRecyclerView.layoutManager = viewManager
        shoppingCartRecyclerView.adapter = CommonVariables.shoppingCartAdapter
    }
}
