package cz.quanti.android.vendor_app.repository.invoice.impl

import cz.quanti.android.vendor_app.repository.invoice.InvoiceFacade
import cz.quanti.android.vendor_app.repository.invoice.InvoiceRepository
import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import cz.quanti.android.vendor_app.repository.invoice.dto.api.InvoiceApiEntity
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import quanti.com.kotlinlog.Log

class InvoiceFacadeImpl(
    private val invoiceRepo: InvoiceRepository
) : InvoiceFacade {

    override fun syncWithServer(vendorId: Int): Completable {
        Log.d(TAG, "Sync started")
        return retrieveInvoices(vendorId)
    }

    override fun getInvoices(): Observable<List<Invoice>> {
        return invoiceRepo.getInvoices()
    }

    override fun deleteInvoices(): Completable {
        return invoiceRepo.deleteInvoices()
    }

    private fun retrieveInvoices(vendorId: Int): Completable {
        return invoiceRepo.retrieveInvoices(vendorId).flatMapCompletable {
            val responseCode = it.first
            val invoicesList = it.second
            if (isPositiveResponseHttpCode(responseCode)) {
                actualizeInvoiceDatabase(invoicesList)
            } else {
                // todo doresit aby exceptiony neprerusovaly sync
                throw VendorAppException("Received code $responseCode when trying download invoices.").apply {
                    apiError = true
                    apiResponseCode = responseCode
                }
            }
        }
    }

    private fun actualizeInvoiceDatabase(invoices: List<InvoiceApiEntity>?): Completable {
        return invoiceRepo.deleteInvoices().andThen(
            Observable.fromIterable(invoices).flatMapCompletable { invoice ->
                Completable.fromSingle(invoiceRepo.saveInvoice(invoice))
            })
    }

    companion object {
        private val TAG = InvoiceFacadeImpl::class.java.simpleName
    }
}
