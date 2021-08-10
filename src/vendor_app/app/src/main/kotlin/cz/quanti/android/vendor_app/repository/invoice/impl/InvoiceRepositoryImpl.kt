package cz.quanti.android.vendor_app.repository.invoice.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.invoice.InvoiceRepository
import cz.quanti.android.vendor_app.repository.invoice.dao.InvoiceDao
import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import cz.quanti.android.vendor_app.repository.invoice.dto.api.InvoiceApiEntity
import cz.quanti.android.vendor_app.repository.invoice.dto.db.InvoiceDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao,
    private val api: VendorAPI
) : InvoiceRepository {

    override fun retrieveInvoices(vendorId: Int): Single<Pair<Int, List<InvoiceApiEntity>>> {
        return api.getInvoices(vendorId).map { response ->
            var invoices = listOf<InvoiceApiEntity>()
            response.body()?.let { invoices = it.data }
            Pair(response.code(), invoices)
        }
    }

    override fun deleteInvoices(): Completable {
        return Completable.fromCallable { invoiceDao.deleteAll()}
    }

    override fun saveInvoice(invoice: InvoiceApiEntity): Single<Long> {
        return Single.fromCallable { invoiceDao.insert(convertToDb(invoice)) }
    }

    override fun getInvoices(): Single<List<Invoice>> {
        return invoiceDao.getAll().flatMap { invoicesDb ->
            Observable.fromIterable(invoicesDb)
                .flatMapSingle { invoiceDb ->
                    val invoice = Invoice(
                        invoiceId = invoiceDb.id,
                        date = invoiceDb.date,
                        quantity = invoiceDb.quantity,
                        value = invoiceDb.value,
                        currency = invoiceDb.currency
                    )
                    Single.just(invoice)
                }.toList()
        }
    }

    private fun convertToDb(invoice: InvoiceApiEntity): InvoiceDbEntity {
        return InvoiceDbEntity(
            id = invoice.id,
            date = invoice.date,
            quantity = invoice.quantity,
            value = invoice.value,
            currency = invoice.currency
        )
    }
}