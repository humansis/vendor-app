package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.adapter.ShopAdapter
import cz.quanti.android.vendor_app.main.vendor.callback.ProductsFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_products.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class ProductsFragment : Fragment(), ProductsFragmentCallback {
    private val vm: VendorViewModel by viewModel()
    private lateinit var adapter: ShopAdapter
    private lateinit var vendorFragmentCallback: VendorFragmentCallback
    private var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vendorFragmentCallback = parentFragment as VendorFragmentCallback
        adapter = ShopAdapter(vendorFragmentCallback, requireContext())
    }

    private fun setAdapter() {
        val viewManager = LinearLayoutManager(activity)

        shopRecyclerView.setHasFixedSize(true)
        shopRecyclerView.layoutManager = viewManager
        shopRecyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).productsFragmentCallback = this
        setAdapter()
        reloadProductsFromDb()
    }

    override fun reloadProductsFromDb() {
        disposable?.dispose()
        disposable =
            vm.getProducts().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { products ->
                        adapter.setData(products)
                    },
                    {
                        Log.e(it)
                    }
                )
    }

    override fun onDestroy() {
        (activity as MainActivity).productsFragmentCallback = null
        disposable?.dispose()
        super.onDestroy()
    }
}
