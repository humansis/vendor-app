package cz.quanti.android.vendor_app.repository.booklet

import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface BookletFacade {

    fun getAllDeactivatedBooklets(): Single<List<Booklet>>

    fun deactivate(booklet: String): Completable

    fun getProtectedBooklets(): Single<List<Booklet>>

    fun syncWithServer(): Observable<SynchronizationSubject>

    fun isSyncNeeded(): Single<Boolean>
}
