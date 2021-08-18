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
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.MainActivity.OnTouchOutsideViewListener
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.shop.adapter.ShopAdapter
import cz.quanti.android.vendor_app.main.shop.viewmodel.ShopViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import io.reactivex.BackpressureStrategy
import kotlinx.android.synthetic.main.fragment_products.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductsFragment : Fragment(), OnTouchOutsideViewListener {

    private val vm: ShopViewModel by viewModel()
    private lateinit var adapter: ShopAdapter

    private var activityCallback: ActivityCallback? = null

    var chosenCurrency: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityCallback = activity as ActivityCallback
        activityCallback?.setToolbarVisible(true)
        activityCallback?.setSubtitle(getString(R.string.products))
        requireActivity().findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.home_button)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // TODO update after product categories are introduced
                    requireActivity().finish()
                }
            }
        )

        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        productsSearchBar.onActionViewCollapsed()
        super.onStop()
    }

    override fun onTouchOutside(view: View?, event: MotionEvent?) {
        if (!productsSearchBar.isIconified) {
            productsSearchBar.clearFocus()
            if (productsSearchBar.query.isEmpty()) {
                productsSearchBar.isIconified = true
            }
        }
    }

    private fun initProductsAdapter() {
        adapter = ShopAdapter(this, requireContext())

        val viewManager = GridLayoutManager(activity, gridColumns())

        productsRecyclerView.setHasFixedSize(true)
        productsRecyclerView.layoutManager = viewManager
        productsRecyclerView.adapter = adapter
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
        productsSearchBar.setOnClickListener {
            productsSearchBar.isIconified = false
        }
        productsSearchBar.imeOptions = EditorInfo.IME_ACTION_DONE
        productsSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        (activity as MainActivity).setOnTouchOutsideViewListener(productsSearchBar, this)
    }

    private fun initObservers() {
        vm.getProducts().toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
            .observe(viewLifecycleOwner, {
                adapter.setData(it)
            })

        vm.getSelectedProducts().observe(viewLifecycleOwner, {
            when (it.size) {
                EMPTY_CART_SIZE -> {
                    cartBadge.visibility = View.GONE
                }
                else -> {
                    cartBadge.visibility = View.VISIBLE
                    cartBadge.text = it.size.toString()
                }
            }
        })

        vm.getCurrency().observe(viewLifecycleOwner, {
            chosenCurrency = it
        })
    }

    private fun initOnClickListeners() {
        cartFAB.setOnClickListener {
            findNavController().navigate(
                ProductsFragmentDirections.actionProductsFragmentToCheckoutFragment()
            )
        }
    }

    fun openProduct(product: Product) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_product, null)

        Glide
            .with(requireContext())
            .load(product.image)
            .into(dialogView.findViewById(R.id.productImage))

        dialogView.findViewById<TextView>(R.id.productName).text = product.name

        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .show()
        if ( !resources.getBoolean(R.bool.isTablet) ) {
            dialog.window?.setLayout(
                resources.displayMetrics.widthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        loadOptions(dialog, dialogView, product)
    }

    private fun loadOptions(dialog: AlertDialog, dialogView: View, product: Product) {
        val priceEditText = dialogView.findViewById<EditText>(R.id.priceEditText)
        val confirmButton = dialogView.findViewById<MaterialButton>(R.id.confirmButton)
        priceEditText.hint = requireContext().getString(R.string.price)
        dialogView.findViewById<TextInputLayout>(R.id.priceTextInputLayout).suffixText = chosenCurrency

        dialogView.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.text = requireContext().getString(R.string.add_to_cart)
        confirmButton.setOnClickListener {
            try {
                val price = priceEditText.text.toString().toDouble()
                if (price <= INVALID_PRICE_VALUE) {
                    showInvalidPriceEnteredMessage()
                } else {
                    addProductToCart(
                        product,
                        price
                    )
                    dialog.dismiss()
                }
            } catch(e: NumberFormatException) {
                showInvalidPriceEnteredMessage()
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

    private fun showInvalidPriceEnteredMessage() {
        Toast.makeText(
            requireContext(),
            requireContext().getString(R.string.please_enter_price),
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        const val EMPTY_CART_SIZE = 0
        const val INVALID_PRICE_VALUE = 0.0
        const val PORTRAIT_PHONE_COLUMNS = 3
        const val PORTRAIT_TABLET_COLUMNS = 4
        const val LANDSCAPE_TABLET_COLUMNS = 6
    }
}
