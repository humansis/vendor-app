package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import org.koin.androidx.viewmodel.ext.android.viewModel

class VendorFragment() : Fragment(), VendorFragmentCallback {
    private val vm: VendorViewModel by viewModel()
    lateinit var chosenCurrency: String

    val args: VendorFragmentArgs by navArgs()

    private val STATE_ONLY_PRODUCTS_SHOWED = 0
    private val STATE_SHOPPING_CART_SHOWED = 1
    private val STATE_PRODUCT_DETAIL_SHOWED = 2


    private var state = STATE_ONLY_PRODUCTS_SHOWED

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.show()
        return inflater.inflate(R.layout.fragment_vendor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        state = vm.getVendorState()
        chosenCurrency = args.currency

        val fragment = getFragmentFromState()

        if (isLandscapeOriented()) {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.rightFragmentContainer, fragment)
                replace(R.id.leftFragmentContainer, ProductsFragment())
            }
            transaction.commit()
        } else {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.vendorSingleFragmentContainer, fragment)
            }
            transaction.commit()
        }
    }

    override fun onResume() {
        super.onResume()

        val toolbar = (activity as MainActivity).supportActionBar
        toolbar?.title = getString(R.string.vendor_title)
        toolbar?.setDisplayHomeAsUpEnabled(false)
        toolbar?.setDisplayShowTitleEnabled(true)
        toolbar?.setDisplayShowCustomEnabled(true)

        (activity as MainActivity).invalidateOptionsMenu()
    }


    override fun chooseProduct(product: Product) {
        val productDetailFragment = ProductDetailFragment()
        productDetailFragment.product = product

        state = STATE_PRODUCT_DETAIL_SHOWED

        if (isLandscapeOriented()) {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.rightFragmentContainer, productDetailFragment)
            }
            transaction.commit()
        } else {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.vendorSingleFragmentContainer, productDetailFragment)
            }
            transaction.commit()
        }
    }

    override fun getCurrency(): String {
        return chosenCurrency
    }

    override fun setCurrency(currency: String) {
        chosenCurrency = currency
    }

    override fun showCart() {
        state = STATE_SHOPPING_CART_SHOWED

        if (!isLandscapeOriented()) {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.vendorSingleFragmentContainer, ShoppingCartFragment())
            }
            transaction.commit()
        } else {
            val shoppingCartFragment = ShoppingCartFragment()
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.rightFragmentContainer, shoppingCartFragment)
            }
            transaction.commit()
        }
    }

    override fun showProducts() {
        state = STATE_ONLY_PRODUCTS_SHOWED

        if (!isLandscapeOriented()) {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.vendorSingleFragmentContainer, ProductsFragment())
            }
            transaction.commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.setVendorState(state)
    }

    private fun isLandscapeOriented(): Boolean {
        return (requireActivity().findViewById<View>(R.id.vendorSingleFragmentContainer) == null)
    }

    private fun getFragmentFromState(): Fragment {
        return when (state) {
            STATE_SHOPPING_CART_SHOWED -> {
                ShoppingCartFragment()
            }
            STATE_PRODUCT_DETAIL_SHOWED -> {
                ProductDetailFragment()
            }
            STATE_ONLY_PRODUCTS_SHOWED -> {
                if (isLandscapeOriented()) {
                    ShoppingCartFragment()
                } else {
                    ProductsFragment()
                }
            }
            else -> ProductsFragment()
        }
    }

}
