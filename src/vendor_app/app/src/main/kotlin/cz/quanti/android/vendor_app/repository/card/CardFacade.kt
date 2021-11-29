package cz.quanti.android.vendor_app.repository.card

import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Observable
import io.reactivex.Single

interface CardFacade {

    fun syncWithServer(): Observable<SynchronizationSubject>

    fun getBlockedCards(): Single<List<String>>
}
