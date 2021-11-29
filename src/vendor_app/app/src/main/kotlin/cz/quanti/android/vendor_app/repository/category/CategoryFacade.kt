package cz.quanti.android.vendor_app.repository.category

import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Observable

interface CategoryFacade {

    fun syncWithServer(
        vendorId: Int
    ): Observable<SynchronizationSubject>
}
