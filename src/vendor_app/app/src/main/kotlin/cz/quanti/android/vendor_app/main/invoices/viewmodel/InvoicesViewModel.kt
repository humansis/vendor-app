package cz.quanti.android.vendor_app.main.invoices.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.invoice.InvoiceFacade
import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import cz.quanti.android.vendor_app.sync.SynchronizationManager
import cz.quanti.android.vendor_app.sync.SynchronizationState
import io.reactivex.Completable
import io.reactivex.Observable

class InvoicesViewModel(
    private val invoiceFacade: InvoiceFacade,
    private val synchronizationManager: SynchronizationManager
) : ViewModel() {

    fun getInvoices(): Observable<List<Invoice>> {
        return invoiceFacade.getInvoices()
    }

    fun syncStateObservable(): Observable<SynchronizationState> {
        return synchronizationManager.syncStateObservable()
    }

    fun deleteInvoices(): Completable {
        return invoiceFacade.deleteInvoices()
    }
}
