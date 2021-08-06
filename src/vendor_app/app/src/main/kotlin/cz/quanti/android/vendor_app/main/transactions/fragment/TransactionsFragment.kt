package cz.quanti.android.vendor_app.main.transactions.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.FragmentTransactionsBinding
import cz.quanti.android.vendor_app.databinding.ItemWarningBinding
import cz.quanti.android.vendor_app.main.transactions.adapter.TransactionsAdapter
import cz.quanti.android.vendor_app.main.transactions.viewmodel.TransactionsViewModel
import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.sync.SynchronizationState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class TransactionsFragment : Fragment() {

    private val vm: TransactionsViewModel by viewModel()
    private lateinit var transactionsAdapter: TransactionsAdapter
    private var syncStartedDisposable: Disposable? = null
    private var unsyncedPurchasesDisposable: Disposable? = null
    private var loadTransactionsDisposable: Disposable? = null

    private lateinit var transactionsBinding: FragmentTransactionsBinding
    private lateinit var warningBinding: ItemWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as ActivityCallback).setTitle(getString(R.string.transactions_to_reimburse))
        transactionsAdapter = TransactionsAdapter(requireContext())
        transactionsBinding = FragmentTransactionsBinding.inflate(inflater)
        warningBinding = ItemWarningBinding.inflate(inflater)
        return transactionsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity)
        transactionsBinding.transactionsRecyclerView.setHasFixedSize(true)
        transactionsBinding.transactionsRecyclerView.layoutManager = viewManager
        transactionsBinding.transactionsRecyclerView.adapter = transactionsAdapter
        warningBinding.root.visibility = View.GONE
        warningBinding.warningButton.setOnClickListener {
            vm.sync()
        }
    }

    override fun onStart() {
        super.onStart()
        initLoadTransactionCheck()
        initSyncStartedCheck()
        initUnsyncedPurchasesCheck()
    }

    private fun initLoadTransactionCheck() {
        transactionsBinding.fragmentMessage.text = getString(R.string.loading)
        loadTransactionsDisposable?.dispose()
        loadTransactionsDisposable =
            vm.syncStateObservable().filter { it == SynchronizationState.SUCCESS }
                .flatMapSingle { vm.getTransactions().map { TransactionsDecorator(it, true) } }
                .startWith(
                    vm.getTransactions().toObservable().map { TransactionsDecorator(it, false) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ transactionsDecorator ->
                    transactionsAdapter.setData(transactionsDecorator.transactions)
                    showMessage()
                    if (transactionsDecorator.hideUnsyncedButton) {
                        warningBinding.root.visibility = View.GONE
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        warningBinding.warningText.text = getString(R.string.unsynced_transactions, it.size)
                        warningBinding.root.visibility = View.VISIBLE
                        warningBinding.warningButton.isEnabled = true
                    } else {
                        warningBinding.root.visibility = View.GONE
                    }
                }, {
                    Log.e(it)
                })
    }

    private fun initSyncStartedCheck() {
        syncStartedDisposable?.dispose()
        syncStartedDisposable =
            vm.syncStateObservable().filter { it == SynchronizationState.STARTED }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    warningBinding.warningButton.isEnabled = false
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
        transactionsBinding.fragmentMessage.text = getString(R.string.no_transactions_to_reimburse)
        if (transactionsAdapter.itemCount == 0) {
            transactionsBinding.fragmentMessage.visibility = View.VISIBLE
        } else {
            transactionsBinding.fragmentMessage.visibility = View.GONE
        }
    }

    private data class TransactionsDecorator(
        val transactions: List<Transaction>,
        val hideUnsyncedButton: Boolean
    )
}
