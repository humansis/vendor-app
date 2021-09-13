package cz.quanti.android.vendor_app.repository.synchronization

import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface SynchronizationFacade {

    fun synchronize(vendor: Vendor): Completable

    fun isSyncNeeded(purchasesCount: Long): Single<Boolean>

    fun getPurchasesCount(): Observable<Long>
}
