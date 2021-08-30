package cz.quanti.android.vendor_app.main.transactions.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.transactions.adapter.TransactionsAdapter
import cz.quanti.android.vendor_app.main.transactions.viewmodel.TransactionsViewModel
import cz.quanti.android.vendor_app.sync.SynchronizationState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.android.synthetic.main.item_warning.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class TransactionsFragment : Fragment() {

    private val vm: TransactionsViewModel by viewModel()
    private lateinit var transactionsAdapter: TransactionsAdapter
    private var purchasesDisposable: Disposable? = null
    private var syncStateDisposable: Disposable? = null
    private var transactionsDisposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as ActivityCallback).setTitle(getString(R.string.transactions_to_reimburse))
        transactionsAdapter = TransactionsAdapter(requireContext())
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity)
        transactions_recycler_view.setHasFixedSize(true)
        transactions_recycler_view.layoutManager = viewManager
        transactions_recycler_view.adapter = transactionsAdapter
        unsynced_warning.visibility = View.GONE
        warning_button.setOnClickListener {
            vm.sync()
        }
    }

    override fun onStart() {
        super.onStart()
        setMessage(getString(R.string.no_transactions_to_reimburse))
        initObservers()
    }

    private fun initObservers() {
        purchasesDisposable?.dispose()
        purchasesDisposable = vm.unsyncedPurchasesSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                warning_text.text = getString(R.string.unsynced_transactions, it.size)
                if (it.isNotEmpty()) {
                    unsynced_warning.visibility = View.VISIBLE
                } else {
                    unsynced_warning.visibility = View.GONE
                }
            },{
                Log.e(TAG, it)
            })

        syncStateDisposable?.dispose()
        syncStateDisposable = vm.syncStateObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ syncState ->
                when (syncState) {
                    SynchronizationState.SUCCESS -> {
                        getTransactions()
                    }
                    SynchronizationState.ERROR -> {
                        warning_button.isEnabled = true
                        setMessage(getString(R.string.no_transactions_to_reimburse))
                    }
                    SynchronizationState.STARTED -> {
                        warning_button.isEnabled = false
                        setMessage(getString(R.string.loading))
                    }
                    else -> {

                    }
                }
            }, {
                Log.e(TAG, it)
            })
    }

    private fun getTransactions() {
        transactionsDisposable?.dispose()
        transactionsDisposable = vm.getTransactions()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                transactionsAdapter.setData(it)
                setMessage(getString(R.string.no_transactions_to_reimburse))
            }, {
                Log.e(TAG, it)
            })
    }


    override fun onStop() {
        super.onStop()
        transactionsDisposable?.dispose()
        syncStateDisposable?.dispose()
    }

    private fun setMessage(message: String) {
        fragment_message.text = message
        if (transactionsAdapter.itemCount == 0) {
            fragment_message.visibility = View.VISIBLE
        } else {
            fragment_message.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = TransactionsFragment::class.java.simpleName
    }
}
