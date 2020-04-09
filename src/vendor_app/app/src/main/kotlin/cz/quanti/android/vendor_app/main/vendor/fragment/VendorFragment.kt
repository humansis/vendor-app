package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.quanti.android.vendor_app.App
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.VendorScreenState
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_product_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log
import java.util.*

class VendorFragment() : Fragment(), VendorFragmentCallback {
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
        (activity as MainActivity).supportActionBar?.show()
        return inflater.inflate(R.layout.fragment_vendor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        state = vm.getVendorState()
        product = getProductFromBundle(savedInstanceState)

        val fragment = getFragmentFromState()

        if (fragment is ProductDetailFragment) {
            fragment.savedQuantity = savedInstanceState?.getString("productQuantityEditText", "")
            fragment.savedUnitPrice = savedInstanceState?.getString("productUnitPriceEditText", "")
        }

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

        var lastSynced = (requireActivity().application as App).preferences.lastSynced
        if (Date().time - lastSynced > rightTimeToSyncAgain && (requireActivity() as MainActivity).isNetworkAvailable()) {
            disposable?.dispose()
            disposable = vm.synchronizeWithServer().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        (requireActivity().application as App).preferences.lastSynced = Date().time
                        notifyDataChanged()
                    },
                    {
                        Log.e(it)
                    }
                )
        }
    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).vendorFragmentCallback = this
        val toolbar = (activity as MainActivity).supportActionBar
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
        val productDetailFragment = ProductDetailFragment()
        this.product = product

        state = VendorScreenState.STATE_PRODUCT_DETAIL_SHOWED

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

    override fun showCart() {
        state = VendorScreenState.STATE_SHOPPING_CART_SHOWED

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
        state = VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED

        if (!isLandscapeOriented()) {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.vendorSingleFragmentContainer, ProductsFragment())
            }
            transaction.commit()
        }
    }

    override fun getSelectedProduct(): Product {
        return product
    }

    override fun notifyDataChanged() {

        if (isLandscapeOriented()) {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.rightFragmentContainer, ShoppingCartFragment())
                replace(R.id.leftFragmentContainer, ProductsFragment())
            }
            transaction.commit()
        } else {
            val transaction = childFragmentManager.beginTransaction().apply {
                replace(R.id.vendorSingleFragmentContainer, ProductsFragment())
            }
            transaction.commit()
        }
    }

    override fun onDestroy() {
        vm.setVendorState(state)
        disposable?.dispose()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        addProductToBundle(outState)
        addEditTextInfoToBundle(outState)
    }

    private fun addProductToBundle(bundle: Bundle) {
        bundle.putLong("productId", product.id)
        bundle.putString("productName", product.name)
        bundle.putString("productImage", product.image)
        bundle.putString("productUnit", product.unit)
    }

    private fun addEditTextInfoToBundle(bundle: Bundle) {

        productDetailFragment?.let {
            bundle.putString("productQuantityEditText", quantityEditText.text.toString())
            bundle.putString("productUnitPriceEditText", unitPriceEditText.text.toString())
        }
    }

    private fun getProductFromBundle(bundle: Bundle?): Product {
        return if (bundle == null) {
            Product()
        } else {
            Product().apply {
                id = bundle.getLong("productId", 0)
                name = bundle.getString("productName", "")
                image = bundle.getString("productImage", "")
                unit = bundle.getString("productUnit", "")
            }
        }
    }

    private fun isLandscapeOriented(): Boolean {
        return requireActivity().findViewById<View>(R.id.vendorSingleFragmentContainer) == null
    }

    private fun getFragmentFromState(): Fragment {
        return when (state) {
            VendorScreenState.STATE_SHOPPING_CART_SHOWED -> {
                ShoppingCartFragment()
            }
            VendorScreenState.STATE_PRODUCT_DETAIL_SHOWED -> {
                ProductDetailFragment()
            }
            VendorScreenState.STATE_ONLY_PRODUCTS_SHOWED -> {
                if (isLandscapeOriented()) {
                    ShoppingCartFragment()
                } else {
                    ProductsFragment()
                }
            }
        }
    }

}
