package cz.quanti.android.vendor_app.repository.card.impl

import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.card.dto.CardPayment
import cz.quanti.android.vendor_app.utils.BlockedCardError
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable

class CardFacadeImpl(private val cardRepo: CardRepository) : CardFacade {

    override fun saveCardPayment(cardPayment: CardPayment): Completable {
        return cardRepo.getBlockedCards().flatMapCompletable { blockedCardsId ->
            if (cardPayment.cardId in blockedCardsId) {
                throw BlockedCardError("This card is tagged as blocked on the server")
            } else {
                cardRepo.saveCardPayment(cardPayment)
            }
        }

    }

    override fun syncWithServer(): Completable {
        return sendCardPurchasesToServer()
            .andThen(clearCardPayments())
            .andThen(actualizeBlockedCardsFromServer())
    }

    private fun sendCardPurchasesToServer(): Completable {
        return cardRepo.getCardPayments().flatMapCompletable { payments ->
            Observable.fromIterable(payments).flatMapCompletable { payment ->
                cardRepo.sendCardPaymentToServer(payment).flatMapCompletable { responseCode ->
                    if (isPositiveResponseHttpCode(responseCode)) {
                        Completable.complete()
                    } else {
                        throw VendorAppException("Could not send card payments to server").apply {
                            apiError = true
                            apiResponseCode = responseCode
                        }
                    }
                }
            }
        }
    }

    private fun clearCardPayments(): Completable {
        return cardRepo.deleteAllCardPayments()
    }

    private fun actualizeBlockedCardsFromServer(): Completable {
        return cardRepo.getBlockedCardsFromServer().flatMapCompletable { response ->
            val responseCode = response.first
            if (isPositiveResponseHttpCode(responseCode)) {
                val blockedCards = response.second
                cardRepo.deleteAllBlockedCards()
                    .andThen(
                        Observable.fromIterable(blockedCards).flatMapCompletable { blockedCard ->
                            cardRepo.saveBlockedCard(blockedCard)
                        })
            } else {
                throw VendorAppException("Could not load blocked cards").apply {
                    this.apiResponseCode = responseCode
                    this.apiError = true
                }
            }
        }
    }
}
