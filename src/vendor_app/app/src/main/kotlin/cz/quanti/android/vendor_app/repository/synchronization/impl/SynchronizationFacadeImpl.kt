package cz.quanti.android.vendor_app.repository.synchronization.impl

import cz.quanti.android.vendor_app.repository.booklet.BookletFacade
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.product.ProductFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import io.reactivex.Completable
import io.reactivex.Single

class SynchronizationFacadeImpl(
    private val bookletFacade: BookletFacade,
    private val cardFacade: CardFacade,
    private val productFacade: ProductFacade,
    private val purchaseFacade: PurchaseFacade
) : SynchronizationFacade {

    override fun synchronize(vendorId: Int): Completable {
        return purchaseFacade.syncWithServer(vendorId)
            .andThen(bookletFacade.syncWithServer())
            .andThen(cardFacade.syncWithServer())
            .andThen(productFacade.syncWithServer())
    }

    override fun isSyncNeeded(): Single<Boolean> {
        return purchaseFacade.isSyncNeeded().flatMap {
            if(it) {
                Single.just(true)
            } else {
                bookletFacade.isSyncNeeded()
            }
        }
    }

    override fun unsyncedPurchases(): Single<List<Purchase>> {
        return purchaseFacade.unsyncedPurchases()
    }
}
