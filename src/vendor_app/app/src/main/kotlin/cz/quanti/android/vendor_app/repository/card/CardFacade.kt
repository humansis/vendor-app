package cz.quanti.android.vendor_app.repository.card

import io.reactivex.Completable
import io.reactivex.Single

interface CardFacade {

    fun syncWithServer(): Completable

    fun getBlockedCards(): Single<List<String>>
}
