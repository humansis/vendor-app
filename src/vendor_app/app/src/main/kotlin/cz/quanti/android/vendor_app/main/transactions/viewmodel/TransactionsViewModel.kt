package cz.quanti.android.vendor_app.main.transactions.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.repository.transaction.TransactionFacade
import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Observable

class TransactionsViewModel(
    private val transactionFacade: TransactionFacade,
    private val synchronizationManager: SynchronizationManager,
    private val synchronizationFacade: SynchronizationFacade
) : ViewModel() {

    fun getTransactions(): Observable<List<Transaction>> {
        return transactionFacade.getTransactions()
    }

    fun syncStateObservable(): Observable<SynchronizationState> {
        return synchronizationManager.syncStateObservable()
    }

    fun getPurchasesCount(): LiveData<Long> {
        return synchronizationFacade.getPurchasesCount().toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
    }

    fun sync() {
        synchronizationManager.synchronizeWithServer()
    }

    fun deleteTransactions(): Completable {
        return transactionFacade.deleteTransactions()
    }
}
