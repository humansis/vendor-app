package cz.quanti.android.vendor_app.main.vendor.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.toLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.MainActivity.OnTouchOutsideViewListener
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.ShopAdapter
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import io.reactivex.BackpressureStrategy
import kotlinx.android.synthetic.main.fragment_products.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductsFragment : Fragment(), OnTouchOutsideViewListener {

    private val vm: VendorViewModel by viewModel()
    private lateinit var adapter: ShopAdapter

    var chosenCurrency: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as ActivityCallback).setToolbarVisible(true)
        (requireActivity() as ActivityCallback).setTitle(getString(R.string.app_name))
        requireActivity().findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.home_button)

        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initProductsAdapter()
        initObservers()
        initSearchBar()
        initOnClickListeners()
    }

    override fun onStop() {
        // colapse searchbar after eventual screen rotation
        productsSearchBar.onActionViewCollapsed()
        super.onStop()
    }

    override fun onTouchOutside(view: View?, event: MotionEvent?) {
        if (view == productsSearchBar) {
            if (productsSearchBar.query.isBlank()) {
                productsSearchBar.onActionViewCollapsed()
            } else {
                productsSearchBar.clearFocus()
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
                resources.getBoolean(R.bool.isTablet) -> 4
                else -> 3
            }
            else -> 6
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
        (activity as MainActivity).setOnTouchOutsideViewListener(
            productsSearchBar,
            this
        )
    }

    private fun initObservers() {
        vm.getProducts().toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
            .observe(viewLifecycleOwner, {
                adapter.setData(it)
            })

        vm.cartSizeLD.observe(viewLifecycleOwner, {
            when (it) {
                0 -> {
                    cartBadge.visibility = View.GONE
                }
                else -> {
                    cartBadge.visibility = View.VISIBLE
                    cartBadge.text = it.toString()
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

        dialogView.findViewById<ImageView>(R.id.productImage).setImageDrawable(product.drawable)

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
                if (price <= 0.0) {
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
                this.currency = vm.getCurrency().value.toString()
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
}
