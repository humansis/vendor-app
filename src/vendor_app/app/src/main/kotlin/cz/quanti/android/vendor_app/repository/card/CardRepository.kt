package cz.quanti.android.vendor_app.repository.card

import cz.quanti.android.vendor_app.repository.card.dto.CardPayment
import io.reactivex.Completable
import io.reactivex.Single

interface CardRepository {

    fun saveCardPayment(cardPayment: CardPayment): Completable

    fun sendCardPaymentToServer(cardPayment: CardPayment): Single<Int>

    fun getCardPayments(): Single<List<CardPayment>>

    fun deleteAllCardPayments(): Completable
}
