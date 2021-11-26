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
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject

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

    private val syncSubjectReplaySubject = ReplaySubject.create<SynchronizationSubject>()

    override fun synchronize(vendor: Vendor): Completable {
        val vendorId = vendor.id.toInt()
        return purchaseFacade.syncWithServer(syncSubjectReplaySubject)
            .andThen(bookletFacade.syncWithServer(syncSubjectReplaySubject))
            .andThen(cardFacade.syncWithServer(syncSubjectReplaySubject))
            .andThen(depositFacade.syncWithServer(syncSubjectReplaySubject, vendorId))
            .andThen(categoryFacade.syncWithServer(syncSubjectReplaySubject, vendorId))
            .andThen(productFacade.syncWithServer(syncSubjectReplaySubject, vendorId))
            .andThen(transactionFacade.syncWithServer(syncSubjectReplaySubject, vendorId))
            .andThen(invoiceFacade.syncWithServer(syncSubjectReplaySubject, vendorId))
    }

    override fun getSyncSubjectObservable(): Observable<SynchronizationSubject> {
        // TODO xxxFacade.getSyncSubject merge vsechny do jednoho
        return syncSubjectReplaySubject
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
