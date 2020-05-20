package cz.quanti.android.vendor_app.repository.card.impl

import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.card.dto.CardPayment
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable

class CardFacadeImpl(private val cardRepo: CardRepository) : CardFacade {

    override fun saveCardPayment(cardPayment: CardPayment): Completable {
        return cardRepo.saveCardPayment(cardPayment)
    }

    override fun syncWithServer(): Completable {
        return cardRepo.getCardPayments().flatMapCompletable { payments ->
            Observable.fromIterable(payments).flatMapCompletable { payment ->
                cardRepo.sendCardPaymentToServer(payment).flatMapCompletable { responseCode ->
                    if (isPositiveResponseHttpCode(responseCode)) {
                        cardRepo.deleteAllCardPayments()
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

    override fun clearCardPayments(): Completable {
        return cardRepo.deleteAllCardPayments()
    }
}
