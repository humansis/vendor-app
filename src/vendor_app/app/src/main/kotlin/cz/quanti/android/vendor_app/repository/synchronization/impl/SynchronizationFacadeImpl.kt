package cz.quanti.android.vendor_app.repository.synchronization.impl

import cz.quanti.android.vendor_app.repository.booklet.BookletFacade
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.category.CategoryFacade
import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.invoice.InvoiceFacade
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.repository.transaction.TransactionFacade
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Observable
import io.reactivex.Single

class SynchronizationFacadeImpl(
    private val bookletFacade: BookletFacade,
    private val cardFacade: CardFacade,
    private val categoryFacade: CategoryFacade,
    private val depositFacade: DepositFacade,
    private val productFacade: ProductFacade,
    private val purchaseFacade: PurchaseFacade,
    private val transactionFacade: TransactionFacade,
    private val invoiceFacade: InvoiceFacade
) : SynchronizationFacade {

    override fun synchronize(vendor: Vendor): Observable<SynchronizationSubject> {
        val vendorId = vendor.id.toInt()
        return purchaseFacade.syncWithServer()
            .concatWith(bookletFacade.syncWithServer())
            .concatWith(cardFacade.syncWithServer())
            .concatWith(depositFacade.syncWithServer(vendorId))
            .concatWith(categoryFacade.syncWithServer(vendorId))
            .concatWith(productFacade.syncWithServer(vendorId))
            .concatWith(transactionFacade.syncWithServer(vendorId))
            .concatWith(invoiceFacade.syncWithServer(vendorId))
    }

    override fun isSyncNeeded(): Observable<Boolean> {
        return getPurchasesCount().flatMapSingle { purchasesCount ->
            if (purchasesCount > 0) {
                Single.just(true)
            } else {
                bookletFacade.isSyncNeeded()
            }
        }
    }

    override fun getPurchasesCount(): Observable<Long> {
        return purchaseFacade.getPurchasesCount()
    }
}
