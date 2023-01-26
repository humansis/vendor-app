package cz.quanti.android.vendor_app.repository.synchronization.impl

import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.category.CategoryFacade
import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.invoice.InvoiceFacade
import cz.quanti.android.vendor_app.repository.log.LogFacade
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.repository.transaction.TransactionFacade
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Observable
import io.reactivex.Single

class SynchronizationFacadeImpl(
    private val cardFacade: CardFacade,
    private val depositFacade: DepositFacade,
    private val categoryFacade: CategoryFacade,
    private val productFacade: ProductFacade,
    private val purchaseFacade: PurchaseFacade,
    private val transactionFacade: TransactionFacade,
    private val invoiceFacade: InvoiceFacade,
    private val logFacade: LogFacade
) : SynchronizationFacade {

    override fun synchronize(vendorId: Int): Observable<SynchronizationSubject> {
        return Observable.concatDelayError(
            listOf(
                purchaseFacade.syncWithServer(),
                depositFacade.syncWithServer(vendorId),
                cardFacade.syncWithServer(),
                categoryFacade.syncWithServer(vendorId)
                    .concatWith(productFacade.syncWithServer(vendorId)),
                transactionFacade.syncWithServer(vendorId),
                invoiceFacade.syncWithServer(vendorId),
                logFacade.syncWithServer(vendorId)
            )
        )
    }

    override fun isSyncNeeded(): Observable<Boolean> {
        return getPurchasesCount().flatMapSingle { purchasesCount ->
            Single.just(purchasesCount > 0)
        }
    }

    override fun getPurchasesCount(): Observable<Long> {
        return purchaseFacade.getPurchasesCount()
    }
}
