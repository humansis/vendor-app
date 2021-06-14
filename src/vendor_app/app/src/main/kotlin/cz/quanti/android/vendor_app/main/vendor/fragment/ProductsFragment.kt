package cz.quanti.android.vendor_app.main.vendor.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.MainActivity.OnTouchOutsideViewListener
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.ShopAdapter
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_products.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class ProductsFragment : Fragment(), OnTouchOutsideViewListener {

    private val vm: VendorViewModel by viewModel()
    private lateinit var adapter: ShopAdapter
    private var reloadProductsDisposable: Disposable? = null

    var chosenCurrency: String = ""

    private val picasso = Picasso.get()

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

        adapter = ShopAdapter(this, requireContext())
    }

    override fun onStart() {
        super.onStart()

        initProductsAdapter()
        initSearchBar()
        initObservers()
        initOnClickListeners()

        reloadProductsDisposable?.dispose()
        reloadProductsDisposable =
            vm.syncNeededObservable().flatMapSingle {
                vm.getProducts()
            }.startWith(vm.getProducts().toObservable())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ products ->
                    adapter.setData(products)
                }, {
                    Log.e(it)
                })
    }

    override fun onStop() {
        reloadProductsDisposable?.dispose()
        super.onStop()
    }

    override fun onTouchOutside(view: View?, event: MotionEvent?) {
        if (view == productsSearchBar) {
            if (productsSearchBar.query.isBlank()) {
                productsSearchBar.isIconified = true
            } else {
                productsSearchBar.clearFocus()
            }
        }
    }

    private fun initProductsAdapter() {
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
        // todo vyresit proc se jakoby vyprazdnuje query pri otoceni obrazovky a proc query obcas neni videt
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
        vm.cartSizeLD.observe(viewLifecycleOwner, Observer {
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

        vm.getCurrency().observe(viewLifecycleOwner, Observer {
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

        picasso.isLoggingEnabled = true
        val img = ImageView(context)
        picasso.load(product.image)
            .into(img, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    dialogView.findViewById<ImageView>(R.id.productImage)?.background = img.drawable
                }

                override fun onError(e: java.lang.Exception?) {
                    Log.e(e?.message ?: "")
                }
            })

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
        priceEditText.hint = requireContext().getString(R.string.price)
        dialogView.findViewById<TextInputLayout>(R.id.priceTextInputLayout).suffixText = chosenCurrency

        dialogView.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.confirmButton).text = requireContext().getString(R.string.add_to_cart)
        dialogView.findViewById<MaterialButton>(R.id.confirmButton).setOnClickListener {
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
