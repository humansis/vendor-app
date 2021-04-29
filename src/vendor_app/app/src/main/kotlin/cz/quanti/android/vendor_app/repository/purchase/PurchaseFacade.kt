package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import io.reactivex.Completable
import io.reactivex.Single

interface PurchaseFacade {

    fun savePurchase(purchase: Purchase): Completable

    fun syncWithServer(vendorId: Int): Completable

    fun isSyncNeeded(): Single<Boolean>

    fun unsyncedPurchases(): Single<List<Purchase>>
}
