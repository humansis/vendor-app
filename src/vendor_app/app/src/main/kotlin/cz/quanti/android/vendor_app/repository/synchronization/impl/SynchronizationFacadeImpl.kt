package cz.quanti.android.vendor_app.repository.synchronization.impl

import cz.quanti.android.vendor_app.repository.booklet.BookletFacade
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.invoice.InvoiceFacade
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import cz.quanti.android.vendor_app.repository.transaction.TransactionFacade
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class SynchronizationFacadeImpl(
    private val bookletFacade: BookletFacade,
    private val cardFacade: CardFacade,
    private val productFacade: ProductFacade,
    private val purchaseFacade: PurchaseFacade,
    private val transactionFacade: TransactionFacade,
    private val invoiceFacade: InvoiceFacade
) : SynchronizationFacade {

    override fun synchronize(vendorId: Int): Completable {
        return purchaseFacade.syncWithServer()
            .andThen(bookletFacade.syncWithServer())
            .andThen(cardFacade.syncWithServer())
            .andThen(productFacade.syncWithServer())
            .andThen(transactionFacade.syncWithServer(vendorId))
            .andThen(invoiceFacade.syncWithServer(vendorId))
    }

    override fun isSyncNeeded(purchasesCount: Long): Single<Boolean> {
        return if(purchasesCount > 0) {
            Single.just(true)
        } else {
            bookletFacade.isSyncNeeded()
        }
    }

    override fun unsyncedPurchases(): Single<List<Purchase>> {
        return purchaseFacade.unsyncedPurchases()
    }

    override fun getPurchasesCount(): Observable<Long> {
        return purchaseFacade.getPurchasesCount()
    }
}
