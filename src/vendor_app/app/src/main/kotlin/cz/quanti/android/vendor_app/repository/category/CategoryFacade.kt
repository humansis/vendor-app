package cz.quanti.android.vendor_app.repository.category

import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.subjects.ReplaySubject

interface CategoryFacade {

    fun syncWithServer(
        syncSubjectReplaySubject: ReplaySubject<SynchronizationSubject>,
        vendorId: Int
    ): Completable
}
