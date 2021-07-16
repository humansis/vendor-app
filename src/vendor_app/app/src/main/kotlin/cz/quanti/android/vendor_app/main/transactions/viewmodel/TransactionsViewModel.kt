package cz.quanti.android.vendor_app.main.transactions.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.repository.transaction.TransactionFacade
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import io.reactivex.Observable
import io.reactivex.Single

class TransactionsViewModel(
    private val transactionFacade: TransactionFacade,
    private val synchronizationManager: SynchronizationManager,
    private val synchronizationFacade: SynchronizationFacade
) : ViewModel() {

    fun getTransactions(): Single<List<Transaction>> {
        return transactionFacade.getTransactions()
    }

    fun syncStateObservable(): Observable<SynchronizationState> {
        return synchronizationManager.syncStateObservable()
    }

    fun unsyncedPurchasesSingle(): Single<List<Purchase>> {
        return synchronizationFacade.unsyncedPurchases()
    }

    fun sync() {
        synchronizationManager.synchronizeWithServer()
    }
}
