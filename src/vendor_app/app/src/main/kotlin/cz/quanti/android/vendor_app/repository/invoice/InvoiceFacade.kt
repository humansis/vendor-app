package cz.quanti.android.vendor_app.repository.invoice

import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface InvoiceFacade {

    fun syncWithServer(
        vendorId: Int
    ): Completable

    fun getInvoices(): Observable<List<Invoice>>

    fun deleteInvoices(): Completable

    fun getSyncSubject(): PublishSubject<SynchronizationSubject>
}
