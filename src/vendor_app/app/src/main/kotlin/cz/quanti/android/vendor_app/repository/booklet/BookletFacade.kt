package cz.quanti.android.vendor_app.repository.booklet

import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject

interface BookletFacade {

    fun getAllDeactivatedBooklets(): Single<List<Booklet>>

    fun deactivate(booklet: String): Completable

    fun getProtectedBooklets(): Single<List<Booklet>>

    fun syncWithServer(syncSubjectReplaySubject: ReplaySubject<SynchronizationSubject>): Completable

    fun isSyncNeeded(): Single<Boolean>
}
