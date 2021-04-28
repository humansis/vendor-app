package cz.quanti.android.vendor_app.main.redeemed_batches.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.R
import quanti.com.kotlinlog.Log

class RedeemedBatchesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_redeemed_batches, container, false)
    }
}
