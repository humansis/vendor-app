package cz.quanti.android.vendor_app.repository.log

import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Observable

interface LogFacade {
    fun syncWithServer(vendorId: Int): Observable<SynchronizationSubject>
}
