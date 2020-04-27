package cz.quanti.android.vendor_app.main.vendor.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.VendorScreenState
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.utils.isNetworkAvailable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log
import java.util.*

class VendorFragment() : Fragment(), VendorFragmentCallback {

    companion object {
        const val PRODUCT_DETAIL_FRAGMENT_TAG = "ProductDetailFragment"
        const val STATE = "state"
    }

    private val vm: VendorViewModel by viewModel()
    var product: Product = Product()
    private val rightTimeToSyncAgain = 86400000 // one day
    private var disposable: Disposable? = null

    private var state = VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.show()
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
        val lastSynced = vm.getLastSynced()
        if (Date().time - lastSynced > rightTimeToSyncAgain && isNetworkAvailable(requireActivity())) {
            disposable?.dispose()
            disposable = vm.synchronizeWithServer().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        vm.setLastSynced(Date().time)
                        initFragments(savedInstanceState)
                    },
                    {
                        initFragments(savedInstanceState)
                        Log.e(it)
                    }
                )
        } else {
            initFragments(savedInstanceState)
        }
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
                    R.id.rightFragmentContainer,
                    productDetailFragment
                )
                replace(R.id.leftFragmentContainer, ProductsFragment())
            }
        } else {
            childFragmentManager.beginTransaction().apply {
                replace(
                    R.id.rightFragmentContainer,
                    productDetailFragment
                )
            }
        }
        transaction.commit()
    }

    private fun showShoppingCart() {
        state = VendorScreenState.STATE_SHOPPING_CART_SHOWED
        val transaction: FragmentTransaction = if (isLandscapeOriented()) {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.rightFragmentContainer, ShoppingCartFragment())
                replace(R.id.leftFragmentContainer, ProductsFragment())
            }
        } else {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.rightFragmentContainer, ShoppingCartFragment())
            }

        }
        transaction.commit()
    }

    private fun showProducts() {
        state = VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED
        val transaction: FragmentTransaction = if (isLandscapeOriented()) {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.rightFragmentContainer, ShoppingCartFragment())
                replace(R.id.leftFragmentContainer, ProductsFragment())
            }
        } else {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.rightFragmentContainer, ProductsFragment())
            }

        }
        transaction.commit()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).vendorFragmentCallback = this
        val toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar?.title = getString(R.string.vendor_title)
        toolbar?.setDisplayHomeAsUpEnabled(false)
        toolbar?.setDisplayShowTitleEnabled(true)
        toolbar?.setDisplayShowCustomEnabled(true)

        (activity as MainActivity).invalidateOptionsMenu()
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
                    R.id.rightFragmentContainer,
                    ProductDetailFragment::class.java,
                    bundle,
                    PRODUCT_DETAIL_FRAGMENT_TAG
                )
            }
            transaction.commit()
        } else {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(
                    R.id.rightFragmentContainer,
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
                replace(R.id.rightFragmentContainer, ProductsFragment())
            }
            transaction.commit()
        } else {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.rightFragmentContainer, ShoppingCartFragment())
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
