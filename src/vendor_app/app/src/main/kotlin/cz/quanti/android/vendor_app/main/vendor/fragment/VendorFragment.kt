package cz.quanti.android.vendor_app.main.vendor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VendorFragment: Fragment() {
    val vm: VendorViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).supportActionBar?.show()
        return inflater.inflate(R.layout.fragment_vendor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val shoppingCartFragment = ShoppingCartFragment()
//        val transaction = activity?.supportFragmentManager?.beginTransaction()?.apply {
//            replace(R.id.fragmentContainer, shoppingCartFragment)
//        }
//        transaction?.commit()

        val productDetailFragment = ProductDetailFragment()
        val transaction = activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, productDetailFragment)
        }
        transaction?.commit()
    }

    override fun onResume() {
        super.onResume()

        val toolbar = (activity as MainActivity).supportActionBar
        toolbar?.title = getString(R.string.vendor_title)
        toolbar?.setDisplayHomeAsUpEnabled(false)
        toolbar?.setDisplayShowTitleEnabled(true)
        toolbar?.setDisplayShowCustomEnabled(true)

        (activity as MainActivity).invalidateOptionsMenu()
    }
}
