package cz.quanti.android.vendor_app.main.vendor.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.VendorScreenState
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.repository.product.dto.Product
import io.reactivex.disposables.Disposable

class VendorFragment() : Fragment(), VendorFragmentCallback {

    companion object {
        const val PRODUCT_DETAIL_FRAGMENT_TAG = "ProductDetailFragment"
        const val STATE = "state"
    }

    var product: Product = Product()
    private val rightTimeToSyncAgain = 86400000 // one day //todo pouzit?
    private var disposable: Disposable? = null

    private var state = VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as ActivityCallback).setToolbarVisible(true)
        requireActivity().findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.home_button)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() { // Handle the back button event
                    when (state) {
                        VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED -> {
                            requireActivity().finish()
                        }
                        VendorScreenState.STATE_SHOPPING_CART_SHOWED -> {
                            showProducts()
                        }
                        VendorScreenState.STATE_PRODUCT_DETAIL_SHOWED -> {
                            showProducts()
                        }
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        return inflater.inflate(R.layout.fragment_vendor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragments(savedInstanceState)
    }

    private fun initFragments(bundle: Bundle?) {
        if (bundle == null) {
            showProducts()
        } else {
            val state = VendorScreenState.valueOf(
                bundle.getString(
                    STATE,
                    VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED.name
                )
            )
            when (state) {
                VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED -> {
                    showProducts()
                }
                VendorScreenState.STATE_SHOPPING_CART_SHOWED -> {
                    showShoppingCart()
                }
                VendorScreenState.STATE_PRODUCT_DETAIL_SHOWED -> {
                    showProductDetail(bundle)
                }
            }
        }
    }

    private fun showProductDetail(bundle: Bundle) {
        state = VendorScreenState.STATE_PRODUCT_DETAIL_SHOWED
        val productDetailFragment = childFragmentManager.getFragment(
            bundle,
            PRODUCT_DETAIL_FRAGMENT_TAG
        ) as ProductDetailFragment

        val transaction: FragmentTransaction = if (isLandscapeOriented()) {
            childFragmentManager.beginTransaction().apply {
                replace(
                    R.id.secondFragmentContainer,
                    productDetailFragment
                )
                replace(R.id.firstFragmentContainer, ProductsFragment())
            }
        } else {
            childFragmentManager.beginTransaction().apply {
                replace(
                    R.id.secondFragmentContainer,
                    productDetailFragment
                )
                replace(R.id.firstFragmentContainer, ProductsFragment())
            }
        }
        transaction.commit()
    }

    private fun showShoppingCart() {
        state = VendorScreenState.STATE_SHOPPING_CART_SHOWED
        val transaction: FragmentTransaction = if (isLandscapeOriented()) {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.secondFragmentContainer, ShoppingCartFragment())
                replace(R.id.firstFragmentContainer, ProductsFragment())
            }
        } else {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.firstFragmentContainer, ProductsFragment())
                replace(R.id.secondFragmentContainer, ShoppingCartFragment())
            }

        }
        transaction.commit()
    }

    private fun showProducts() {
        state = VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED
        val transaction: FragmentTransaction = if (isLandscapeOriented()) {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.secondFragmentContainer, ShoppingCartFragment())
                replace(R.id.firstFragmentContainer, ProductsFragment())
            }
        } else {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.firstFragmentContainer, ProductsFragment())
                replace(R.id.secondFragmentContainer, ShoppingCartFragment())
            }

        }
        transaction.commit()
    }

    override fun onStop() {
        (activity as MainActivity).vendorFragmentCallback = null
        super.onStop()
    }


    override fun chooseProduct(product: Product) {
        state = VendorScreenState.STATE_PRODUCT_DETAIL_SHOWED
        val bundle = Bundle().apply {
            putLong(ProductDetailFragment.ID, product.id)
            putString(ProductDetailFragment.NAME, product.name)
            putString(ProductDetailFragment.IMAGE, product.image)
            putString(ProductDetailFragment.UNIT, product.unit)
        }
        if (isLandscapeOriented()) {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(
                    R.id.secondFragmentContainer,
                    ProductDetailFragment::class.java,
                    bundle,
                    PRODUCT_DETAIL_FRAGMENT_TAG
                )
            }
            transaction.commit()
        } else {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(
                    R.id.secondFragmentContainer,
                    ProductDetailFragment::class.java,
                    bundle,
                    PRODUCT_DETAIL_FRAGMENT_TAG
                )
            }
            transaction.commit()
        }
    }

    override fun showCart() {
        showShoppingCart()
    }

    override fun backToProducts() {
        state = VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED

        if (!isLandscapeOriented()) {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.firstFragmentContainer, ProductsFragment())
                replace(R.id.secondFragmentContainer, ShoppingCartFragment())
            }
            transaction.commit()
        } else {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.secondFragmentContainer, ShoppingCartFragment())
            }
            transaction.commit()
        }
    }

    override fun getSelectedProduct(): Product {
        return product
    }

    override fun notifyDataChanged() {
        showProducts()
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val productDetailFragment = childFragmentManager.findFragmentByTag(PRODUCT_DETAIL_FRAGMENT_TAG)
        productDetailFragment?.let {
            childFragmentManager.putFragment(outState, PRODUCT_DETAIL_FRAGMENT_TAG, it)
        }
        outState.putString(STATE, state.name)
    }

    private fun isLandscapeOriented(): Boolean {
        return requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}
