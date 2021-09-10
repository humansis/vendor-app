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
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import io.reactivex.BackpressureStrategy
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.sync.SynchronizationState
import cz.quanti.android.vendor_app.utils.getBackgroundColor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import quanti.com.kotlinlog.Log
import java.math.BigDecimal
import kotlin.math.abs

class ShopFragment : Fragment(), OnTouchOutsideViewListener {

    private val loginVM: LoginViewModel by viewModel()
    private val mainVM: MainViewModel by sharedViewModel()
    private val vm: ShopViewModel by viewModel()
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var shopBinding: FragmentShopBinding
    private lateinit var activityCallback: ActivityCallback
    private var syncStateDisposable: Disposable? = null
    private var categoriesAllowed = MutableLiveData<Boolean>()
    private lateinit var appBarState: AppBarStateEnum
    private var chosenCurrency: String = ""
    private var allProducts: List<Product> = listOf()
    private var selectedProducts: List<SelectedProduct> = listOf()

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
                        showCategories(true, null)
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
        shopBinding.categoriesAppBarLayout.background.setTint(
            getBackgroundColor(requireContext(), loginVM.getApiHost())
        )
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
            Log.d(TAG, "SearchBar clicked")
            if (shopBinding.shopSearchBar.isIconified) {
                shopBinding.shopSearchBar.isIconified = false
                productsAdapter.filter.filter("")
            }
            showCategories(false, null)
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
                    appBarState = AppBarStateEnum.EXPANDED
                } else {
                    categoriesAllowed.value = false
                    appBarState = AppBarStateEnum.COLLAPSED
                }
            })

        categoriesAllowed.observe(viewLifecycleOwner, {
            setAppBarHidden(!it)
            showCategories(it, null)
            if (it) {
                shopBinding.categoriesAppBarLayout.addOnOffsetChangedListener( OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                        if (appBarState != AppBarStateEnum.COLLAPSED) {
                            appBarState = AppBarStateEnum.COLLAPSED
                            shopBinding.categoriesRecyclerView.scrollToPosition(0)
                        }
                    } else {
                        appBarState = AppBarStateEnum.EXPANDED
                    }
                })
            }
        })

        syncStateDisposable?.dispose()
        syncStateDisposable = vm.syncStateObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ syncState ->
                when (syncState) {
                    SynchronizationState.STARTED -> {
                        setMessage(getString(R.string.loading))
                    }
                    else -> {
                        setMessage(getString(R.string.no_products))
                    }
                }
            }, {
                Log.e(it)
            })

        vm.getProducts().toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
            .observe(viewLifecycleOwner, { products ->
                allProducts = products
                productsAdapter.setData(chosenCurrency, products)
                setMessageVisible(products.isEmpty())
            })

        vm.getSelectedProductsLD().observe(viewLifecycleOwner, { products ->
            selectedProducts = products
            when (products.size) {
                EMPTY_CART_SIZE -> {
                    shopBinding.totalTextView.visibility = View.GONE
                    shopBinding.cartFAB.visibility = View.GONE
                    shopBinding.cartBadge.visibility = View.GONE

                }
                else -> {
                    actualizeTotal(products.map { it.price }.sum())
                    shopBinding.totalTextView.visibility = View.VISIBLE
                    shopBinding.cartFAB.visibility = View.VISIBLE
                    shopBinding.cartBadge.visibility = View.VISIBLE
                    shopBinding.cartBadge.text = products.size.toString()
                }
            }
        })

        vm.getCurrency().observe(viewLifecycleOwner, { currency ->
            chosenCurrency = currency
            productsAdapter.setData(currency, allProducts)
            actualizeTotal(selectedProducts.map { it.price }.sum())
            showCategories(true, null)
        })
    }

    private fun initOnClickListeners() {
        shopBinding.cartFAB.setOnClickListener {
            Log.d(TAG, "Cart FAB clicked")
            findNavController().navigate(
                ShopFragmentDirections.actionProductsFragmentToCheckoutFragment()
            )
        }

        shopBinding.productsHeader.setOnClickListener {
            Log.d(TAG, "Cart products header clicked")
            shopBinding.productsRecyclerView.stopScroll()
            clearQuery()
            showCategories(!shopBinding.categoriesAppBarLayout.isAppBarExpanded(), null)
        }
    }

    private fun setAppBarHidden(boolean: Boolean) {
        if (boolean) {
            shopBinding.categoriesAppBarLayout.layoutParams.height = 0
        } else {
            shopBinding.categoriesAppBarLayout.layoutParams.height = AppBarLayout.LayoutParams.WRAP_CONTENT
        }
    }

    fun openCategory(category: Category) {
        if (category.type != CategoryType.ALL) {
            productsAdapter.filter.filter(category.name)
        } else {
            clearQuery()
        }
        showCategories(false, category.name)
    }

    private fun showCategories(boolean: Boolean, name: String?) {
        shopBinding.categoriesAppBarLayout.setExpanded(boolean)
        shopBinding.productsHeader.text = name ?: getString(R.string.all_products)
    }

    fun openProduct(product: Product, productLayout: View) {
        val hasCashback = selectedProducts.find { it.product.category.type == CategoryType.CASHBACK }
        if (hasCashback != null && product.category.type == CategoryType.CASHBACK) {
            mainVM.setToastMessage(getString(R.string.only_one_cashback_item_allowed))
            productLayout.isEnabled = true
        } else {
            val dialogBinding = DialogProductBinding.inflate(layoutInflater,null, false)

            Glide
                .with(requireContext())
                .load(product.image)
                .into(dialogBinding.productImage)

            dialogBinding.productName.text = product.name

            val dialog = AlertDialog.Builder(activity)
                .setView(dialogBinding.root)
                .show()
            dialog.setOnDismissListener {
                productLayout.isEnabled = true
            }
            if (!resources.getBoolean(R.bool.isTablet)) {
                dialog.window?.setLayout(
                    resources.displayMetrics.widthPixels,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            loadOptions(dialog, dialogBinding, product)
        }
    }

    private fun loadOptions(dialog: AlertDialog, dialogBinding: DialogProductBinding, product: Product) {
        val priceEditText = dialogBinding.editProduct.priceEditText
        val confirmButton = dialogBinding.editProduct.confirmButton
        priceEditText.hint = requireContext().getString(R.string.price)
        dialogBinding.editProduct.priceTextInputLayout.suffixText = chosenCurrency

        if (product.category.type == CategoryType.CASHBACK) {
            dialogBinding.editProduct.priceEditText.isEnabled = false
            product.unitPrice?.let {
                val price = BigDecimal.valueOf(it).stripTrailingZeros().toPlainString()
                dialogBinding.editProduct.priceEditText.setText(price.toString())
            }
        }

        dialogBinding.closeButton.setOnClickListener {
            Log.d(TAG, "Close product options button clicked")
            dialog.dismiss()
        }

        confirmButton.text = requireContext().getString(R.string.add_to_cart)
        confirmButton.setOnClickListener {
            Log.d(TAG, "Confirm product options clicked")
            try {
                val price = priceEditText.text.toString().toDouble()
                when {
                    price <= INVALID_PRICE_VALUE -> {
                        mainVM.setToastMessage(getString(R.string.please_enter_price))
                    }
                    else -> {
                        addProductToCart(
                            product,
                            price
                        )
                        dialog.dismiss()
                    }
                }
            } catch (e: NumberFormatException) {
                mainVM.setToastMessage(getString(R.string.please_enter_price))
            }
        }
    }

    private fun addProductToCart(product: Product, unitPrice: Double) {
        val selected = SelectedProduct(
            product = product,
            price = product.unitPrice ?: unitPrice
        )
        vm.addToShoppingCart(selected)
    }

    private fun setMessage(message: String) {
        shopBinding.shopMessage.text = message
    }

    private fun setMessageVisible (visible: Boolean) {
        if (visible) {
            shopBinding.shopMessage.visibility = View.VISIBLE
        } else {
            shopBinding.shopMessage.visibility = View.GONE
        }
    }

    private fun actualizeTotal(total: Double) {
        val totalText = "${getString(R.string.total)}: ${getStringFromDouble(total)} ${vm.getCurrency().value}"
        shopBinding.totalTextView.text = totalText
    }

    companion object {
        private val TAG = ShopFragment::class.java.simpleName
        const val EMPTY_CART_SIZE = 0
        const val INVALID_PRICE_VALUE = 0.0
        const val PORTRAIT_PHONE_COLUMNS = 3
        const val PORTRAIT_TABLET_COLUMNS = 4
        const val LANDSCAPE_TABLET_COLUMNS = 6
    }
}

enum class AppBarStateEnum {
    COLLAPSED, EXPANDED
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
