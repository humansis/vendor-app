package cz.quanti.android.vendor_app.repository.synchronization

import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Observable

interface SynchronizationFacade {

    fun synchronize(vendor: Vendor): Completable

    fun getSyncSubjectObservable(): Observable<SynchronizationSubject>

    fun isSyncNeeded(): Observable<Boolean>

    fun getPurchasesCount(): Observable<Long>
}
