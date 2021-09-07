package cz.quanti.android.vendor_app.main.invoices.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainNavigationDirections
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.FragmentInvoicesBinding
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.invoices.adapter.InvoicesAdapter
import cz.quanti.android.vendor_app.main.invoices.viewmodel.InvoicesViewModel
import cz.quanti.android.vendor_app.sync.SynchronizationState
import cz.quanti.android.vendor_app.utils.getBackgroundColor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class InvoicesFragment : Fragment() {
    private val loginVM: LoginViewModel by viewModel()
    private val vm: InvoicesViewModel by viewModel()
    private lateinit var invoicesAdapter: InvoicesAdapter
    private lateinit var invoicesBinding: FragmentInvoicesBinding
    private var syncStateDisposable: Disposable? = null
    private var invoicesDisposable: Disposable? = null
    private lateinit var activityCallback: ActivityCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = activity as ActivityCallback
        activityCallback.setSubtitle(getString(R.string.reimbursed_invoices))
        invoicesBinding = FragmentInvoicesBinding.inflate(inflater, container, false)
        invoicesAdapter = InvoicesAdapter(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(
                        MainNavigationDirections.actionToProductsFragment()
                    )
                }
            }
        )

        return invoicesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity)
        invoicesBinding.invoicesRecyclerView.setHasFixedSize(true)
        invoicesBinding.invoicesRecyclerView.layoutManager = viewManager
        invoicesBinding.invoicesRecyclerView.adapter = invoicesAdapter
        val color = getBackgroundColor(requireContext(), loginVM.getApiHost())
        invoicesBinding.shadowTop.background.setTint(color)
        invoicesBinding.shadowBottom.background.setTint(color)
    }

    override fun onStart() {
        super.onStart()
        initObservers()
    }

    private fun initObservers() {
        invoicesDisposable?.dispose()
        invoicesDisposable = vm.getInvoices()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ invoices ->
                invoicesAdapter.setData(invoices)
                setMessage(getString(R.string.no_reimbursed_invoices))
                setMessageVisible(invoices.isEmpty())
            }, {
                Log.e(TAG, it)
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
                        setMessage(getString(R.string.no_reimbursed_invoices))
                    }
                }
            }, {
                Log.e(it)
            })
    }

    override fun onStop() {
        super.onStop()
        invoicesDisposable?.dispose()
        syncStateDisposable?.dispose()
    }

    private fun setMessage(message: String) {
        invoicesBinding.invoicesMessage.text = message
    }

    private fun setMessageVisible (visible: Boolean) {
        if (visible) {
            invoicesBinding.invoicesMessage.visibility = View.VISIBLE
        } else {
            invoicesBinding.invoicesMessage.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = InvoicesFragment::class.java.simpleName
    }
}
