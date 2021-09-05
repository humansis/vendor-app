package cz.quanti.android.vendor_app.main.shop.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.MainActivity.OnTouchOutsideViewListener
import cz.quanti.android.vendor_app.MainViewModel
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.DialogProductBinding
import cz.quanti.android.vendor_app.databinding.FragmentShopBinding
import cz.quanti.android.vendor_app.main.shop.adapter.CategoriesAdapter
import cz.quanti.android.vendor_app.main.shop.adapter.ProductsAdapter
import cz.quanti.android.vendor_app.main.shop.viewmodel.ShopViewModel
import cz.quanti.android.vendor_app.repository.category.Category
import cz.quanti.android.vendor_app.repository.category.CategoryType
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import io.reactivex.BackpressureStrategy
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView

class ShopFragment : Fragment(), OnTouchOutsideViewListener {

    private val mainVM: MainViewModel by sharedViewModel()
    private val vm: ShopViewModel by viewModel()
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var shopBinding: FragmentShopBinding
    private lateinit var activityCallback: ActivityCallback
    private var chosenCurrency: String = ""
    private var categoriesAllowed = MutableLiveData<Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = requireActivity() as ActivityCallback
        activityCallback.setToolbarVisible(true)
        activityCallback.setSubtitle(getString(R.string.products))
        activityCallback.getNavView().setCheckedItem(R.id.shop_button)

        shopBinding = FragmentShopBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!shopBinding.categoriesAppBarLayout.isAppBarExpanded() && categoriesAllowed.value == true) {
                        clearQuery()
                        openCategories()
                    } else {
                        requireActivity().finish()
                    }
                }
            }
        )

        return shopBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shopBinding.productsMessage.text = getString(R.string.loading)
        initCategoriesAdapter()
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
        // collapse searchbar after eventual screen rotation
        shopBinding.shopSearchBar.onActionViewCollapsed()
        super.onStop()
    }

    override fun onTouchOutside(view: View?, event: MotionEvent?) {
        if (!shopBinding.shopSearchBar.isIconified) {
            shopBinding.shopSearchBar.clearFocus()
            if (shopBinding.shopSearchBar.query.isEmpty()) {
                shopBinding.shopSearchBar.isIconified = true
            }
        }
    }

    private fun clearQuery() {
        productsAdapter.filter.filter("")
        shopBinding.shopSearchBar.clearFocus()
        shopBinding.shopSearchBar.setQuery("", true)
        shopBinding.shopSearchBar.isIconified = true
    }

    private fun initCategoriesAdapter() {
        categoriesAdapter = CategoriesAdapter(this, requireContext())

        val viewManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false )
        //val viewManager = GridLayoutManager(activity, gridColumns()) TODO poresit s jakubem jestli je linear lepsi ?

        shopBinding.categoriesRecyclerView.setHasFixedSize(true)
        shopBinding.categoriesRecyclerView.layoutManager = viewManager
        shopBinding.categoriesRecyclerView.adapter = categoriesAdapter
    }

    private fun initProductsAdapter() {
        productsAdapter = ProductsAdapter(this, requireContext())

        val viewManager = GridLayoutManager(activity, gridColumns())

        shopBinding.productsRecyclerView.setHasFixedSize(true)
        shopBinding.productsRecyclerView.layoutManager = viewManager
        shopBinding.productsRecyclerView.adapter = productsAdapter
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
        shopBinding.shopSearchBar.setOnClickListener {
            shopBinding.shopSearchBar.isIconified = false
            hideCategories(null)
        }
        shopBinding.shopSearchBar.imeOptions = EditorInfo.IME_ACTION_DONE
        shopBinding.shopSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                productsAdapter.filter.filter(newText)
                return false
            }
        })

        (activity as MainActivity).setOnTouchOutsideViewListener(shopBinding.shopSearchBar, this)
    }

    private fun initObservers() {
        vm.getCategories().toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
            .observe(viewLifecycleOwner, { categories ->
                if (categories.size > 1) {
                    categoriesAllowed.value = true
                    categoriesAdapter.setData(categories.addAllCategory(requireContext()))
                } else {
                    categoriesAllowed.value = false
                }
            })

        categoriesAllowed.observe(viewLifecycleOwner, {
            if (it) {
                openCategories()
                shopBinding.categoriesAppBarLayout.visibility = View.VISIBLE
            } else {
                hideCategories(null)
                shopBinding.productsHeader.visibility = View.GONE // TODO vyresit proc se to neschovava
                shopBinding.categoriesAppBarLayout.visibility = View.GONE
            }
        })

        vm.getProducts().toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
            .observe(viewLifecycleOwner, {
                productsAdapter.setData(it)
                setMessage()
            })

        vm.getSelectedProducts().observe(viewLifecycleOwner, { products ->
            when (products.size) {
                EMPTY_CART_SIZE -> {
                    shopBinding.cartBadge.visibility = View.GONE
                    shopBinding.totalTextView.visibility = View.GONE
                }
                else -> {
                    actualizeTotal(products.map { it.price }.sum())
                    shopBinding.totalTextView.visibility = View.VISIBLE
                    shopBinding.cartBadge.visibility = View.VISIBLE
                    shopBinding.cartBadge.text = products.size.toString()
                }
            }
        })

        vm.getCurrency().observe(viewLifecycleOwner, {
            chosenCurrency = it
        })
    }

    private fun initOnClickListeners() {
        shopBinding.cartFAB.setOnClickListener {
            findNavController().navigate(
                ShopFragmentDirections.actionProductsFragmentToCheckoutFragment()
            )
        }

        shopBinding.productsHeader.setOnClickListener {
            shopBinding.productsRecyclerView.setScrollState(RecyclerView.SCROLL_STATE_IDLE)
            clearQuery()
            if (shopBinding.categoriesAppBarLayout.isAppBarExpanded()) {
                hideCategories(null)
            } else {
                openCategories()
            }
        }
    }

    fun openCategory(category: Category) {
        if (category.type != CategoryType.ALL) {
            productsAdapter.filter.filter(category.name)
        } else {
            clearQuery()
        }
        hideCategories(category.name)
    }

    private fun openCategories() {
        shopBinding.categoriesAppBarLayout.setExpanded(true)
        shopBinding.productsHeader.text = getString(R.string.all_products)
    }

    private fun hideCategories(name: String?) {
        shopBinding.categoriesAppBarLayout.setExpanded(false)
        shopBinding.productsHeader.text = name ?: getString(R.string.all_products)
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
        shopBinding.productsMessage.text = getString(R.string.no_products)
        if (productsAdapter.itemCount == 0) {
            shopBinding.productsMessage.visibility = View.VISIBLE
        } else {
            shopBinding.productsMessage.visibility = View.GONE
        }
    }

    private fun actualizeTotal(total: Double) {
        val totalText = "${getString(R.string.total)}: ${getStringFromDouble(total)} ${vm.getCurrency().value}"
        shopBinding.totalTextView.text = totalText
    }

    companion object {
        const val EMPTY_CART_SIZE = 0
        const val INVALID_PRICE_VALUE = 0.0
        const val PORTRAIT_PHONE_COLUMNS = 3
        const val PORTRAIT_TABLET_COLUMNS = 4
        const val LANDSCAPE_TABLET_COLUMNS = 6
    }
}

private fun List<Category>.addAllCategory(context: Context): List<Category> {
    return this.toMutableList().apply {
        add(0, Category(0,
            context.getString(R.string.all_products),
            CategoryType.ALL
        ))
    }
}

private fun AppBarLayout.isAppBarExpanded(): Boolean {
    val behavior = (this.layoutParams as CoordinatorLayout.LayoutParams).behavior
    return if (behavior is AppBarLayout.Behavior) behavior.topAndBottomOffset == 0 else false
}

private fun RecyclerView.setScrollState(state: Int) {
    val method = this::class.java.getDeclaredMethod("setScrollState", Int::class.java)
    method.isAccessible = true
    method.invoke(this, state)
}
