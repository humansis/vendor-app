package cz.quanti.android.vendor_app.repository.invoice

import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import io.reactivex.Completable
import io.reactivex.Single

interface InvoiceFacade {

    fun syncWithServer(vendorId: Int): Completable

    fun getInvoices(): Single<List<Invoice>>
}
