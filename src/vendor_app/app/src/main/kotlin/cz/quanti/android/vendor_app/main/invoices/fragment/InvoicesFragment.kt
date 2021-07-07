package cz.quanti.android.vendor_app.main.invoices.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.invoices.adapter.InvoicesAdapter
import cz.quanti.android.vendor_app.main.invoices.viewmodel.InvoicesViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_invoices.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class InvoicesFragment : Fragment() {

    private val vm: InvoicesViewModel by viewModel()
    private lateinit var invoicesAdapter: InvoicesAdapter
    private var synchronizeInvoicesDisposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as ActivityCallback).setTitle(getString(R.string.reimbursed_invoices))
        invoicesAdapter = InvoicesAdapter(requireContext())
        return inflater.inflate(R.layout.fragment_invoices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity)
        invoices_recycler_view.setHasFixedSize(true)
        invoices_recycler_view.layoutManager = viewManager
        invoices_recycler_view.adapter = invoicesAdapter
    }

    override fun onStart() {
        super.onStart()
        synchronizeInvoicesDisposable?.dispose()
        synchronizeInvoicesDisposable = vm.syncNeededObservable().flatMapSingle {
            vm.getInvoices()
        }.startWith(vm.getInvoices().toObservable())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ invoices ->
                invoicesAdapter.setData(invoices)
                invoicesAdapter.notifyDataSetChanged()
                showMessage()
            }, {
                Log.e(it)
            })
    }

    override fun onStop() {
        super.onStop()
        synchronizeInvoicesDisposable?.dispose()
    }

    private fun showMessage() {
        if (fragment_message != null) {
            fragment_message.text = getString(R.string.no_reimbursed_invoices)
            if (invoicesAdapter.itemCount == 0) {
                fragment_message.visibility = View.VISIBLE
            } else {
                fragment_message.visibility = View.GONE
            }
        }
    }
}
