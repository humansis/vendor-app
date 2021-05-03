package cz.quanti.android.vendor_app.main.invoices.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.quanti.android.vendor_app.R

class InvoicesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_invoices, container, false)
    }

    //todo onsynclistener ?

    //todo do nadpisu dat celkovy pocet invoicu
}
