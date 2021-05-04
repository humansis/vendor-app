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
import cz.quanti.android.vendor_app.repository.purchase.dto.Transaction
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
    private var syncStartedDisposable: Disposable? = null
    private var unsyncedPurchasesDisposable: Disposable? = null
    private var loadTransactionsDisposable: Disposable? = null

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
        initSyncStartedCheck()
        initUnsyncedPurchasesCheck()
        initLoadTransactionCheck()
    }

    private fun initLoadTransactionCheck() {
        loadTransactionsDisposable?.dispose()
        loadTransactionsDisposable =
            vm.syncStateObservable().filter { it == SynchronizationState.SUCCESS }
                .flatMapSingle { vm.getTransactions().map { TransactionsDecorator(it, true) } }
                .startWith(
                    vm.getTransactions().toObservable().map { TransactionsDecorator(it, false) })
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ transactionsDecorator ->
                    transactionsAdapter.setData(transactionsDecorator.transactions)
                    transactionsAdapter.notifyDataSetChanged()
                    showMessage()
                    if (transactionsDecorator.hideUnsyncedButton) {
                        unsynced_warning.visibility = View.GONE
                    }
                },
                    {
                        Log.e(it)
                    })
    }

    private fun initUnsyncedPurchasesCheck() {
        unsyncedPurchasesDisposable?.dispose()
        unsyncedPurchasesDisposable =
            vm.syncStateObservable().filter { it == SynchronizationState.ERROR }
                .flatMapSingle { vm.unsyncedPurchasesSingle() }
                .startWith(vm.unsyncedPurchasesSingle().toObservable())
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        warning_text.text = getString(R.string.unsynced_transactions, it.size)
                        unsynced_warning.visibility = View.VISIBLE
                        warning_button.isEnabled = true
                    } else {
                        unsynced_warning.visibility = View.GONE
                    }
                }, {
                    Log.e(it)
                })
    }

    private fun initSyncStartedCheck() {
        syncStartedDisposable?.dispose()
        syncStartedDisposable =
            vm.syncStateObservable().filter { it == SynchronizationState.STARTED }
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    warning_button.isEnabled = false
                }, {
                    Log.e(it)
                })
    }

    override fun onStop() {
        super.onStop()
        syncStartedDisposable?.dispose()
        unsyncedPurchasesDisposable?.dispose()
        loadTransactionsDisposable?.dispose()
    }

    private fun showMessage() {
        fragment_message.text = getString(R.string.no_transactions_to_reimburse)
        if (transactionsAdapter.itemCount == 0) {
            fragment_message.visibility = View.VISIBLE
        } else {
            fragment_message.visibility = View.GONE
        }
    }

    private data class TransactionsDecorator(
        val transactions: List<Transaction>,
        val hideUnsyncedButton: Boolean
    )
}
