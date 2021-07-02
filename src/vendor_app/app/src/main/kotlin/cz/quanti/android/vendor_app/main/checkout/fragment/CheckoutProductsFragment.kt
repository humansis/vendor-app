package cz.quanti.android.vendor_app.main.checkout.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.adapter.SelectedProductsAdapter
import cz.quanti.android.vendor_app.main.checkout.callback.CheckoutFragmentCallback
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import kotlinx.android.synthetic.main.fragment_checkout.*
import kotlinx.android.synthetic.main.item_checkout_products_footer.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CheckoutProductsFragment : Fragment() {
    private val vm: CheckoutViewModel by viewModel()

    private val selectedProductsAdapter = SelectedProductsAdapter()
    private lateinit var checkoutFragmentCallback: CheckoutFragmentCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as ActivityCallback).setToolbarVisible(true)
        return inflater.inflate(R.layout.fragment_checkout_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkoutFragmentCallback = parentFragment as CheckoutFragmentCallback
        vm.init()

        initSelectedProductsAdapter()
    }

    private fun initSelectedProductsAdapter() {
        val viewManager = LinearLayoutManager(activity)

        checkoutSelectedProductsRecyclerView?.setHasFixedSize(true)
        checkoutSelectedProductsRecyclerView?.layoutManager = viewManager
        checkoutSelectedProductsRecyclerView?.adapter = selectedProductsAdapter
        selectedProductsAdapter.chosenCurrency = vm.getCurrency()

        selectedProductsAdapter.setData(vm.getShoppingCart())
    }
}
