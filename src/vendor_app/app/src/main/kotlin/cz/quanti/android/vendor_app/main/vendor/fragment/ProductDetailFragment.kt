package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.CurrencyAdapter
import cz.quanti.android.vendor_app.main.vendor.viewmodel.ProductDetailViewModel
import kotlinx.android.synthetic.main.fragment_product_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductDetailFragment: Fragment() {

    private val vm: ProductDetailViewModel by viewModel()
    private var currencyAdapter: CurrencyAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPriceUnitSpinner()
        initOnClickListeners()
    }

    private fun initOnClickListeners() {
        cartButtonImageView.setOnClickListener {
            val shoppingCartFragment = ShoppingCartFragment()
            val transaction = activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragmentContainer, shoppingCartFragment)
            }
            transaction?.commit()
        }
    }

    private fun initPriceUnitSpinner() {
        currencyAdapter = context?.let { CurrencyAdapter(it) }
        currencyAdapter?.init(vm.getSupportedCurrencies())
        currencyAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        priceUnitSpinner.adapter = currencyAdapter
    }
}
