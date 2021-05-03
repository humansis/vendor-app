package cz.quanti.android.vendor_app.main.transactions.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.transactions.adapter.TransactionsAdapter
import cz.quanti.android.vendor_app.main.transactions.callback.TransactionsFragmentCallback
import cz.quanti.android.vendor_app.main.transactions.viewmodel.TransactionsViewModel
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.android.synthetic.main.fragment_transactions.fragment_message
import kotlinx.android.synthetic.main.item_warning.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class TransactionsFragment : Fragment(), TransactionsFragmentCallback {

    private val syncFacade: SynchronizationFacade by inject()
    private val vm: TransactionsViewModel by viewModel()
    private lateinit var transactionsAdapter: TransactionsAdapter
    private var disposable: Disposable? = null
    private var isRecyclerEmpty = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as ActivityCallback).setTitle(getString(R.string.transactions_to_reimburse))
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unsynced_warning.visibility = View.GONE
        fragment_message.text = getString(R.string.no_transactions_to_reimburse)
        showMessage()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        transactionsAdapter = TransactionsAdapter(context)
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).transactionsFragmentCallback = this
        setAdapter()
        reloadTransactionsFromDb()
    }

    private fun setAdapter() {
        val viewManager = LinearLayoutManager(activity)

        transactions_recycler_view.setHasFixedSize(true)
        transactions_recycler_view.layoutManager = viewManager
        transactions_recycler_view.adapter = transactionsAdapter
    }

    override fun notifyDataChanged() {
        parentFragmentManager.beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
        reloadTransactionsFromDb()
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

    private fun reloadTransactionsFromDb() {
        disposable?.dispose()
        disposable =
            vm.getTransactions().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { transactions ->
                        if (transactions.isNotEmpty()) {
                            isRecyclerEmpty = false
                            //todo vyresit proc se to nenaplnuje
                            transactionsAdapter.setData(transactions)
                        } else {
                            isRecyclerEmpty = true
                        }
                    },
                    {
                        Log.e(it)
                    }
                )
    }

    private fun showMessage() {
        if (isRecyclerEmpty) {
            fragment_message.visibility = View.VISIBLE
        } else {
            fragment_message.visibility = View.GONE
        }
    }
}
