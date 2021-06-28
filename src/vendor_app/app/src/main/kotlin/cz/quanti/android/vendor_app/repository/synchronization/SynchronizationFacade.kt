package cz.quanti.android.vendor_app.repository.synchronization

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface SynchronizationFacade {

    fun synchronize(vendorId: Int): Completable

    fun isSyncNeeded(purchasesCount: Long): Single<Boolean>

    fun unsyncedPurchases(): Single<List<Purchase>>

    fun getPurchasesCount(): Observable<Long>
}
