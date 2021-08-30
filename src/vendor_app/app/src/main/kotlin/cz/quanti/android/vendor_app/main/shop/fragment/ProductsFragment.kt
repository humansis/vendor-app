package cz.quanti.android.vendor_app.main.shop.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.toLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.MainActivity.OnTouchOutsideViewListener
import cz.quanti.android.vendor_app.MainViewModel
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.DialogProductBinding
import cz.quanti.android.vendor_app.databinding.FragmentProductsBinding
import cz.quanti.android.vendor_app.main.shop.adapter.ShopAdapter
import cz.quanti.android.vendor_app.main.shop.viewmodel.ShopViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import io.reactivex.BackpressureStrategy
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductsFragment : Fragment(), OnTouchOutsideViewListener {

    private val mainVM: MainViewModel by sharedViewModel()
    private val vm: ShopViewModel by viewModel()
    private lateinit var productsAdapter: ShopAdapter
    private lateinit var productsBinding: FragmentProductsBinding
    private lateinit var activityCallback: ActivityCallback
    private var chosenCurrency: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = requireActivity() as ActivityCallback
        activityCallback.setToolbarVisible(true)
        activityCallback.setSubtitle(getString(R.string.app_name))
        activityCallback.getNavView().setCheckedItem(R.id.shop_button)

        productsBinding = FragmentProductsBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // TODO update after product categories are introduced
                    requireActivity().finish()
                }
            }
        )

        return productsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productsBinding.productsMessage.text = getString(R.string.loading)
        initProductsAdapter()
        initObservers()
        initSearchBar()
        initOnClickListeners()
    }

    override fun onPause() {
        (activity as MainActivity).setOnTouchOutsideViewListener(null, null)
        super.onPause()
    }

    override fun onStop() {
        // colapse searchbar after eventual screen rotation
        productsBinding.productsSearchBar.onActionViewCollapsed()
        super.onStop()
    }

    override fun onTouchOutside(view: View?, event: MotionEvent?) {
        if (!productsBinding.productsSearchBar.isIconified) {
            productsBinding.productsSearchBar.clearFocus()
            if (productsBinding.productsSearchBar.query.isEmpty()) {
                productsBinding.productsSearchBar.isIconified = true
            }
        }
    }

    private fun initProductsAdapter() {
        productsAdapter = ShopAdapter(this, requireContext())

        val viewManager = GridLayoutManager(activity, gridColumns())

        productsBinding.productsRecyclerView.setHasFixedSize(true)
        productsBinding.productsRecyclerView.layoutManager = viewManager
        productsBinding.productsRecyclerView.adapter = productsAdapter
    }

    private fun gridColumns(): Int {
        return when {
            resources.getBoolean(R.bool.isPortrait) -> when {
                resources.getBoolean(R.bool.isTablet) -> PORTRAIT_TABLET_COLUMNS
                else -> PORTRAIT_PHONE_COLUMNS
            }
            else -> LANDSCAPE_TABLET_COLUMNS
        }
    }

    private fun initSearchBar() {
        productsBinding.productsSearchBar.setOnClickListener {
            productsBinding.productsSearchBar.isIconified = false
        }
        productsBinding.productsSearchBar.imeOptions = EditorInfo.IME_ACTION_DONE
        productsBinding.productsSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                productsAdapter.filter.filter(newText)
                return false
            }
        })

        (activity as MainActivity).setOnTouchOutsideViewListener(productsBinding.productsSearchBar, this)
    }

    private fun initObservers() {
        vm.getProducts().toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
            .observe(viewLifecycleOwner, {
                productsAdapter.setData(it)
                setMessage()
            })

        vm.getSelectedProducts().observe(viewLifecycleOwner, { products ->
            when (products.size) {
                EMPTY_CART_SIZE -> {
                    productsBinding.cartBadge.visibility = View.GONE
                    productsBinding.totalTextView.visibility = View.GONE
                }
                else -> {
                    actualizeTotal(products.map { it.price }.sum())
                    productsBinding.totalTextView.visibility = View.VISIBLE
                    productsBinding.cartBadge.visibility = View.VISIBLE
                    productsBinding.cartBadge.text = products.size.toString()
                }
            }
        })

        vm.getCurrency().observe(viewLifecycleOwner, {
            chosenCurrency = it
        })
    }

    private fun initOnClickListeners() {
        productsBinding.cartFAB.setOnClickListener {
            findNavController().navigate(
                ProductsFragmentDirections.actionProductsFragmentToCheckoutFragment()
            )
        }
    }

    fun openProduct(product: Product) {
        val dialogBinding = DialogProductBinding.inflate(layoutInflater,null, false)

        Glide
            .with(requireContext())
            .load(product.image)
            .into(dialogBinding.productImage)

        dialogBinding.productName.text = product.name

        val dialog = AlertDialog.Builder(activity)
            .setView(dialogBinding.root)
            .show()
        if (!resources.getBoolean(R.bool.isTablet)) {
            dialog.window?.setLayout(
                resources.displayMetrics.widthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        loadOptions(dialog, dialogBinding, product)
    }

    private fun loadOptions(dialog: AlertDialog, dialogBinding: DialogProductBinding, product: Product) {
        val priceEditText = dialogBinding.editProduct.priceEditText
        val confirmButton = dialogBinding.editProduct.confirmButton
        priceEditText.hint = requireContext().getString(R.string.price)
        dialogBinding.editProduct.priceTextInputLayout.suffixText = chosenCurrency

        dialogBinding.closeButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.text = requireContext().getString(R.string.add_to_cart)
        confirmButton.setOnClickListener {
            try {
                val price = priceEditText.text.toString().toDouble()
                if (price <= INVALID_PRICE_VALUE) {
                    mainVM.setToastMessage(getString(R.string.please_enter_price))
                } else {
                    addProductToCart(
                        product,
                        price
                    )
                    dialog.dismiss()
                }
            } catch (e: NumberFormatException) {
                mainVM.setToastMessage(getString(R.string.please_enter_price))
            }
        }
    }

    private fun addProductToCart(product: Product, unitPrice: Double) {
        val selected = SelectedProduct()
            .apply {
                this.product = product
                this.price = unitPrice
            }
        vm.addToShoppingCart(selected)
    }

    private fun setMessage() {
        productsBinding.productsMessage.text = getString(R.string.no_products)
        if (productsAdapter.itemCount == 0) {
            productsBinding.productsMessage.visibility = View.VISIBLE
        } else {
            productsBinding.productsMessage.visibility = View.GONE
        }
    }

    private fun actualizeTotal(total: Double) {
        val totalText = "${getString(R.string.total)}: ${getStringFromDouble(total)} ${vm.getCurrency().value}"
        productsBinding.totalTextView.text = totalText
    }

    companion object {
        const val EMPTY_CART_SIZE = 0
        const val INVALID_PRICE_VALUE = 0.0
        const val PORTRAIT_PHONE_COLUMNS = 3
        const val PORTRAIT_TABLET_COLUMNS = 4
        const val LANDSCAPE_TABLET_COLUMNS = 6
    }
}
