package cz.quanti.android.vendor_app.repository.card

import cz.quanti.android.vendor_app.repository.card.dto.CardPayment
import io.reactivex.Completable

interface CardFacade {

    fun saveCardPayment(cardPayment: CardPayment): Completable

    fun syncWithServer(): Completable

    fun clearCardPayments(): Completable
}
