package cz.quanti.android.vendor_app.repository.card

import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface CardFacade {

    fun syncWithServer(): Completable

    fun getBlockedCards(): Single<List<String>>

    fun getSyncSubject(): PublishSubject<SynchronizationSubject>
}
