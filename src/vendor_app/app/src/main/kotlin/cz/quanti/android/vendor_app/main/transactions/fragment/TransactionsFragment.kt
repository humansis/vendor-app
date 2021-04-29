package cz.quanti.android.vendor_app.main.transactions.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.callback.ProductsFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import extensions.isNetworkConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.android.synthetic.main.item_warning.*
import org.koin.android.ext.android.inject
import quanti.com.kotlinlog.Log
import java.util.*

class TransactionsFragment : Fragment() {

    private val syncFacade: SynchronizationFacade by inject()
    private var disposable: Disposable? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onResume() {
        super.onResume()

        disposable?.dispose()
        disposable = syncFacade.unsyncedPurchases()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.isNotEmpty()) {
                        warning_text.text = getString(R.string.unsynced_transactions, it.size)
                        unsynced_warning.visibility = View.VISIBLE
                    } else {
                        unsynced_warning.visibility = View.GONE
                    }
                },
                {
                }
            )

        warning_button.setOnClickListener {
            warning_button.isEnabled = false
            (requireActivity() as ActivityCallback).sync()
        }
    }
}
