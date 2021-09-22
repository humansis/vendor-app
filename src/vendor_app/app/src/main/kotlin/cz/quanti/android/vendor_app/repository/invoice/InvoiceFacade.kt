package cz.quanti.android.vendor_app.repository.invoice

import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import io.reactivex.Completable
import io.reactivex.Observable

interface InvoiceFacade {

    fun syncWithServer(vendorId: Int): Completable

    fun getInvoices(): Observable<List<Invoice>>

    fun deleteInvoices(): Completable
}
