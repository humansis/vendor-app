package cz.quanti.android.vendor_app.repository.synchronization

import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Observable

interface SynchronizationFacade {

    fun synchronize(vendorId: Int): Observable<SynchronizationSubject>

    fun isSyncNeeded(): Observable<Boolean>

    fun getPurchasesCount(): Observable<Long>
}
