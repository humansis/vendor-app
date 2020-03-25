package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.CurrencyAdapter
import cz.quanti.android.vendor_app.main.vendor.misc.CommonVariables
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.SelectedProduct
import kotlinx.android.synthetic.main.fragment_product_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductDetailFragment(private val product: Product) : Fragment() {

    private val vm: VendorViewModel by viewModel()
    private var currencyAdapter: CurrencyAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPriceUnitSpinner()
        initOnClickListeners()
        initProductRelatedInfo()

        if (CommonVariables.cart.isEmpty()) {
            priceUnitSpinner.visibility = View.VISIBLE
            priceUnitTextView.visibility = View.INVISIBLE
        } else {
            priceUnitSpinner.visibility = View.INVISIBLE
            priceUnitTextView.visibility = View.VISIBLE
            priceUnitTextView.text = CommonVariables.choosenCurrency
        }
    }

    private fun initOnClickListeners() {
        cartButtonImageView.setOnClickListener {

            if (quantityEditText.text.toString() != "" && unitPriceEditText.text.toString() != "") {
                if (CommonVariables.cart.isEmpty()) {
                    CommonVariables.choosenCurrency = priceUnitSpinner.selectedItem as String
                }
                addProductToCart(
                    product,
                    quantityEditText.text.toString().toDouble(),
                    unitPriceEditText.text.toString().toDouble()
                )
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
        val selected = SelectedProduct().apply {
            this.product = product
            this.quantity = quantity
            this.price = unitPrice
            this.subTotal = unitPrice * quantity
            this.currency = CommonVariables.choosenCurrency
        }
        CommonVariables.cart.add(selected)
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
