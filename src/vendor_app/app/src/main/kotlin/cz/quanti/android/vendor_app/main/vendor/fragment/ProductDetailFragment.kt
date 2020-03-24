package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.CurrencyAdapter
import cz.quanti.android.vendor_app.main.vendor.adapter.ShoppingCartAdapter
import cz.quanti.android.vendor_app.main.vendor.misc.CommonVariables
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.SelectedProduct
import kotlinx.android.synthetic.main.fragment_product_detail.*
import kotlinx.android.synthetic.main.fragment_shopping_cart.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductDetailFragment(private val product: Product): Fragment() {

    private val vm: VendorViewModel by viewModel()
    private var currencyAdapter: CurrencyAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPriceUnitSpinner()
        initOnClickListeners()
        initProductRelatedInfo()
    }

    private fun initOnClickListeners() {
        cartButtonImageView.setOnClickListener {

            if(quantityEditText.text.toString() != "" && unitPriceEditText.text.toString() != "") {
                if(CommonVariables.choosenCurrency == "") {
                    CommonVariables.choosenCurrency = priceUnitSpinner.selectedItem as String
                    // TODO disable spinner
                }
                addProductToCart(product, quantityEditText.text.toString().toDouble(), unitPriceEditText.text.toString().toDouble())
            }

            // TODO ask if want to leave
            val shoppingCartFragment = ShoppingCartFragment()
            val transaction = activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragmentContainer, shoppingCartFragment)
            }
            transaction?.commit()
        }
    }

    private fun addProductToCart(product: Product, quantity: Double, unitPrice: Double) {
        val selected = SelectedProduct().apply{
            this.product = product
            this.quantity = quantity
            this.price = unitPrice
            this.subTotal = unitPrice * quantity
            this.currency = CommonVariables.choosenCurrency
        }
        CommonVariables.shoppingCartAdapter.add(selected)
    }

    private fun initPriceUnitSpinner() {
        currencyAdapter = context?.let { CurrencyAdapter(it) }
        currencyAdapter?.init(vm.getFirstCurrencies())
        currencyAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        priceUnitSpinner.adapter = currencyAdapter
    }

    private fun initProductRelatedInfo() {
        productName.text = product.name
        quantityUnitTextView.text = product.unit
    }
}
