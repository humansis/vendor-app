package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.MainActivity.OnTouchOutsideViewListener
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.ShopAdapter
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as ActivityCallback).setToolbarVisible(true)
        (requireActivity() as ActivityCallback).setTitle(getString(R.string.app_name))
        requireActivity().findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.home_button)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (!adapter.closeExpandedCard()) {
                        requireActivity().finish()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ShopAdapter(vm, requireContext())
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
        val viewManager = LinearLayoutManager(activity)

        productsRecyclerView.setHasFixedSize(true)
        productsRecyclerView.layoutManager = viewManager
        productsRecyclerView.adapter = adapter
        adapter.chosenCurrency = vm.getCurrency().value.toString()
    }

    private fun initSearchBar() {
        // todo vyresit proc se jakoby vyprazdnuje query pri otoceni obrazovky
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
            adapter.chosenCurrency = it
            adapter.closeExpandedCard()
        })
    }

    private fun initOnClickListeners() {
        cartFAB.setOnClickListener {
            findNavController().navigate(
                ProductsFragmentDirections.actionProductsFragmentToCheckoutFragment()
            )
        }
    }
}
