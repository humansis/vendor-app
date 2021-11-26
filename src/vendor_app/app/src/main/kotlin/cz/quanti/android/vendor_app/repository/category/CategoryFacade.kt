package cz.quanti.android.vendor_app.repository.category

import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject

interface CategoryFacade {

    fun syncWithServer(
        vendorId: Int
    ): Completable

    fun getSyncSubject(): PublishSubject<SynchronizationSubject>
}
