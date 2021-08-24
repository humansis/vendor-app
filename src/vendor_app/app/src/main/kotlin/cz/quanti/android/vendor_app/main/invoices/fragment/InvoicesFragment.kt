package cz.quanti.android.vendor_app.main.invoices.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.FragmentInvoicesBinding
import cz.quanti.android.vendor_app.main.invoices.adapter.InvoicesAdapter
import cz.quanti.android.vendor_app.main.invoices.viewmodel.InvoicesViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class InvoicesFragment : Fragment() {

    private val vm: InvoicesViewModel by viewModel()
    private lateinit var invoicesAdapter: InvoicesAdapter
    private lateinit var invoicesBinding: FragmentInvoicesBinding
    private var synchronizeInvoicesDisposable: Disposable? = null
    private var activityCallback: ActivityCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = activity as ActivityCallback
        activityCallback?.setSubtitle(getString(R.string.reimbursed_invoices))
        invoicesBinding = FragmentInvoicesBinding.inflate(inflater, container, false)
        invoicesAdapter = InvoicesAdapter(requireContext())
        return invoicesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity)
        invoicesBinding.invoicesRecyclerView.setHasFixedSize(true)
        invoicesBinding.invoicesRecyclerView.layoutManager = viewManager
        invoicesBinding.invoicesRecyclerView.adapter = invoicesAdapter
    }

    override fun onStart() {
        super.onStart()
        invoicesBinding.invoicesMessage.text = getString(R.string.loading)
        synchronizeInvoicesDisposable?.dispose()
        synchronizeInvoicesDisposable = vm.syncNeededObservable().flatMapSingle {
            vm.getInvoices()
        }.startWith(vm.getInvoices().toObservable())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ invoices ->
                invoicesAdapter.setData(invoices)
                setMessage()
            }, {
                Log.e(it)
            })
    }

    override fun onStop() {
        super.onStop()
        synchronizeInvoicesDisposable?.dispose()
    }

    private fun setMessage() {
        invoicesBinding.invoicesMessage.text = getString(R.string.no_reimbursed_invoices)
        if (invoicesAdapter.itemCount == 0) {
            invoicesBinding.invoicesMessage.visibility = View.VISIBLE
        } else {
            invoicesBinding.invoicesMessage.visibility = View.GONE
        }
    }
}
