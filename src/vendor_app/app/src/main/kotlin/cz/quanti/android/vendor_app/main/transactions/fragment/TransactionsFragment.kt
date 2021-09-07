package cz.quanti.android.vendor_app.main.transactions.fragment

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
import cz.quanti.android.vendor_app.databinding.FragmentTransactionsBinding
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.transactions.adapter.TransactionsAdapter
import cz.quanti.android.vendor_app.main.transactions.viewmodel.TransactionsViewModel
import cz.quanti.android.vendor_app.sync.SynchronizationState
import cz.quanti.android.vendor_app.utils.getBackgroundColor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class TransactionsFragment : Fragment() {
    private val loginVM: LoginViewModel by viewModel()
    private val vm: TransactionsViewModel by viewModel()
    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var transactionsBinding: FragmentTransactionsBinding
    private var syncStateDisposable: Disposable? = null
    private var transactionsDisposable: Disposable? = null
    private lateinit var activityCallback: ActivityCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = requireActivity() as ActivityCallback
        activityCallback.setSubtitle(getString(R.string.transactions_to_reimburse))
        transactionsAdapter = TransactionsAdapter(requireContext())
        transactionsBinding = FragmentTransactionsBinding.inflate(inflater)

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

        return transactionsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity)
        transactionsBinding.transactionsRecyclerView.setHasFixedSize(true)
        transactionsBinding.transactionsRecyclerView.layoutManager = viewManager
        transactionsBinding.transactionsRecyclerView.adapter = transactionsAdapter
        transactionsBinding.unsyncedWarning.root.visibility = View.GONE
        transactionsBinding.unsyncedWarning.warningButton.setOnClickListener {
            Log.d(TAG, "Sync button clicked")
            vm.sync()
        }
        val color = getBackgroundColor(requireContext(), loginVM.getApiHost())
        transactionsBinding.shadowTop.background.setTint(color)
        transactionsBinding.shadowBottom.background.setTint(color)
    }

    override fun onStart() {
        super.onStart()
        initObservers()
    }

    private fun initObservers() {
        vm.getPurchasesCount().observe(viewLifecycleOwner, {
            transactionsBinding.unsyncedWarning.warningText.text = getString(R.string.unsynced_transactions, it)
            if (it > 0L ) {
                transactionsBinding.unsyncedWarning.root.visibility = View.VISIBLE
            } else {
                transactionsBinding.unsyncedWarning.root.visibility = View.GONE
            }
        })

        transactionsDisposable?.dispose()
        transactionsDisposable = vm.getTransactions()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ transactions ->
                transactionsAdapter.setData(transactions)
                setMessage(getString(R.string.no_transactions_to_reimburse))
                setMessageVisible(transactions.isEmpty())
            }, {
                Log.e(TAG, it)
            })

        syncStateDisposable?.dispose()
        syncStateDisposable = vm.syncStateObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ syncState ->
                when (syncState) {
                    SynchronizationState.ERROR -> {
                        transactionsBinding.unsyncedWarning.warningButton.isEnabled = true
                        setMessage(getString(R.string.no_transactions_to_reimburse))
                    }
                    SynchronizationState.STARTED -> {
                        transactionsBinding.unsyncedWarning.warningButton.isEnabled = false
                        setMessage(getString(R.string.loading))
                    }
                    else -> {}
                }
                setMessageVisible(transactionsAdapter.itemCount == 0)
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
        transactionsBinding.transactionsMessage.text = message
    }

    private fun setMessageVisible (visible: Boolean) {
        if (visible) {
            transactionsBinding.transactionsMessage.visibility = View.VISIBLE
        } else {
            transactionsBinding.transactionsMessage.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = TransactionsFragment::class.java.simpleName
    }
}
