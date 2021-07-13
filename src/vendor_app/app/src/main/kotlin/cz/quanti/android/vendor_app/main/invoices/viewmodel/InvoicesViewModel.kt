package cz.quanti.android.vendor_app.main.invoices.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import io.reactivex.Observable
import io.reactivex.Single

class InvoicesViewModel(
    private val purchaseFacade: PurchaseFacade,
    private val synchronizationManager: SynchronizationManager
) : ViewModel() {

    fun getInvoices() : Single<List<Invoice>> {
        return purchaseFacade.getInvoices()
    }

    fun syncNeededObservable(): Observable<SynchronizationState> {
        return synchronizationManager.syncStateObservable()
            .filter { it == SynchronizationState.SUCCESS }
    }
}
