package cz.quanti.android.vendor_app.repository.synchronization

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import io.reactivex.Completable
import io.reactivex.Single

interface SynchronizationFacade {

    fun synchronize(vendor: Int): Completable

    fun isSyncNeeded(): Single<Boolean>

    fun unsyncedPurchases(): Single<List<Purchase>>
}
