package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.CurrencyAdapter
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import kotlinx.android.synthetic.main.fragment_product_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductDetailFragment(private val product: Product) : Fragment() {

    private val vm: VendorViewModel by viewModel()
    private var currencyAdapter: CurrencyAdapter? = null
    private lateinit var vendorFragmentCallback: VendorFragmentCallback

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

        vendorFragmentCallback = parentFragment as VendorFragmentCallback

        if (vendorFragmentCallback.getShoppingCart().isEmpty()) {
            priceUnitSpinner.visibility = View.VISIBLE
            priceUnitTextView.visibility = View.INVISIBLE
        } else {
            priceUnitSpinner.visibility = View.INVISIBLE
            priceUnitTextView.visibility = View.VISIBLE
            priceUnitTextView.text = vendorFragmentCallback.getCurrency()
        }
    }

    private fun initOnClickListeners() {
        cartButtonImageView.setOnClickListener {

            if (quantityEditText.text.toString() != "" && unitPriceEditText.text.toString() != "") {
                if (vendorFragmentCallback.getShoppingCart().isEmpty()) {
                    vendorFragmentCallback.setCurrency(priceUnitSpinner.selectedItem as String)
                }
                addProductToCart(
                    product,
                    quantityEditText.text.toString().toDouble(),
                    unitPriceEditText.text.toString().toDouble()
                )
                goToCart()
            } else {
                AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                    .setTitle(getString(R.string.areYouSureDialogTitle))
                    .setMessage(getString(R.string.leaveProductDetailDialogMessage))
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        goToCart()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            }
        }
    }

    private fun goToCart() {
        val shoppingCartFragment = ShoppingCartFragment()
        val transaction = parentFragment?.childFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, shoppingCartFragment)
        }
        transaction?.commit()
    }

    private fun addProductToCart(product: Product, quantity: Double, unitPrice: Double) {
        val selected = SelectedProduct()
            .apply {
            this.product = product
            this.quantity = quantity
            this.price = unitPrice
            this.subTotal = unitPrice * quantity
                this.currency = vendorFragmentCallback.getCurrency()
        }
        vendorFragmentCallback.addToShoppingCart(selected)
    }

    private fun initPriceUnitSpinner() {
        currencyAdapter = CurrencyAdapter(requireContext())
        currencyAdapter?.init(vm.getFirstCurrencies())
        currencyAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        priceUnitSpinner.adapter = currencyAdapter
    }

    private fun initProductRelatedInfo() {
        productName.text = product.name
        quantityUnitTextView.text = product.unit
    }
}
