package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.CurrencyAdapter
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import kotlinx.android.synthetic.main.fragment_product_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class ProductDetailFragment : Fragment() {

    companion object {
        const val ID = "productId"
        const val NAME = "productName"
        const val IMAGE = "productImage"
        const val UNIT = "productUnit"
        private val TAG = ProductDetailFragment::class.java.simpleName
    }

    private val vm: VendorViewModel by viewModel()
    private var currencyAdapter: CurrencyAdapter? = null
    private lateinit var vendorFragmentCallback: VendorFragmentCallback
    private lateinit var product: Product
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vendorFragmentCallback = parentFragment as VendorFragmentCallback
        if (savedInstanceState != null) {
            init(savedInstanceState)
        } else {
            arguments?.let {
                init(it)
            }
        }
    }

    fun init(bundle: Bundle) {
        product = Product().apply {
            this.id = bundle.getLong(ID)
            this.name = bundle.getString(NAME, "")
            this.unit = bundle.getString(UNIT, "")
            this.image = bundle.getString(IMAGE, "")
        }

        initPriceUnitSpinner()
        initOnClickListeners()
        initProductRelatedInfo()

        if (vm.getShoppingCart().isEmpty()) {
            priceUnitSpinner.visibility = View.VISIBLE
            priceUnitTextView.visibility = View.INVISIBLE
        } else {
            priceUnitSpinner.visibility = View.INVISIBLE
            priceUnitTextView.visibility = View.VISIBLE
            priceUnitTextView.text = vm.getCurrency()
        }
    }


    private fun initOnClickListeners() {
        addToCartButton.setOnClickListener {
            Log.d(TAG, "Add to cart button clicked.")
            try {
                val price = unitPriceEditText.text.toString().toDouble()
                if (price <= 0.0) {
                    showInvalidPriceEnteredMessage()
                } else {
                    if (vm.getShoppingCart().isEmpty()) {
                        vm.setCurrency(priceUnitSpinner.selectedItem as String)
                        vm.setLastCurrencySelection(priceUnitSpinner.selectedItem as String)
                    }
                    addProductToCart(
                        product,
                        price
                    )
                    goToCart()
                }
            } catch(e: NumberFormatException) {
                showInvalidPriceEnteredMessage()
            }
        }

        backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked.")
            goToProducts()
        }
    }

    private fun showInvalidPriceEnteredMessage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.please_enter_price),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(ID, product.id)
        outState.putString(NAME, product.name)
        outState.putString(IMAGE, product.image)
        outState.putString(UNIT, product.unit)
    }

    private fun goToCart() {
        vendorFragmentCallback.showCart()
    }

    private fun goToProducts() {
        vendorFragmentCallback.backToProducts()
    }

    private fun addProductToCart(product: Product, unitPrice: Double) {
        val selected = SelectedProduct()
            .apply {
                this.product = product
                this.price = unitPrice
                this.currency = vm.getCurrency()
            }
        vm.addToShoppingCart(selected)
    }

    private fun initPriceUnitSpinner() {
        currencyAdapter = CurrencyAdapter(requireContext())
        currencyAdapter?.init(vm.getFirstCurrencies())
        currencyAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        priceUnitSpinner.adapter = currencyAdapter
        priceUnitSpinner.setSelection(
            currencyAdapter?.getPosition(vm.getLastCurrencySelection()) ?: 0
        )
    }

    private fun initProductRelatedInfo() {
        productName.text = product.name
        Picasso.get().load(product.image).into(productImageView)
    }
}
