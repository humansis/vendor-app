package cz.quanti.android.vendor_app.main.checkout.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.viewmodel.CheckoutViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CheckoutFragment: Fragment() {

    private val vm: CheckoutViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).supportActionBar?.show()
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }
}
