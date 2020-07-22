package cz.quanti.android.vendor_app.repository.card

import io.reactivex.Completable

interface CardFacade {

    fun syncWithServer(): Completable
}
