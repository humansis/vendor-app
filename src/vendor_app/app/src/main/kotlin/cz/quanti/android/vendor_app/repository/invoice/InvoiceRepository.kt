package cz.quanti.android.vendor_app.repository.invoice

import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import cz.quanti.android.vendor_app.repository.invoice.dto.api.InvoiceApiEntity
import io.reactivex.Completable
import io.reactivex.Single

interface InvoiceRepository {

    fun retrieveInvoices(vendorId: Int): Single<Pair<Int, List<InvoiceApiEntity>>>

    fun deleteInvoices(): Completable

    fun saveInvoice(invoice: InvoiceApiEntity): Single<Long>

    fun getInvoices(): Single<List<Invoice>>
}
