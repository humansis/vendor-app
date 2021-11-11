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
import io.reactivex.Completable
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

    override fun synchronize(vendor: Vendor): Completable {
        return purchaseFacade.syncWithServer()
            .andThen(bookletFacade.syncWithServer())
            .andThen(cardFacade.syncWithServer())
            .andThen(depositFacade.syncWithServer(vendor.id.toInt()))
            .andThen(categoryFacade.syncWithServer(vendor.id.toInt()))
            .andThen(productFacade.syncWithServer(vendor))
            .andThen(transactionFacade.syncWithServer(vendor.id.toInt()))
            .andThen(invoiceFacade.syncWithServer(vendor.id.toInt()))
    }

    override fun isSyncNeeded(purchasesCount: Long): Single<Boolean> {
        return if (purchasesCount > 0) {
            Single.just(true)
        } else {
            bookletFacade.isSyncNeeded()
        }
    }

    override fun getPurchasesCount(): Observable<Long> {
        return purchaseFacade.getPurchasesCount()
    }
}
