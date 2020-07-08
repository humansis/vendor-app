package cz.quanti.android.vendor_app.repository.card.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.card.dao.BlockedCardDao
import cz.quanti.android.vendor_app.repository.card.dao.CardPaymentDao
import cz.quanti.android.vendor_app.repository.card.dto.CardPayment
import cz.quanti.android.vendor_app.repository.card.dto.api.CardPaymentApiEntity
import cz.quanti.android.vendor_app.repository.card.dto.db.BlockedCardDbEntity
import cz.quanti.android.vendor_app.repository.card.dto.db.CardPaymentDbEntity
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

class CardRepositoryImpl(
    private val cardPaymentDao: CardPaymentDao,
    private val blockedCardDao: BlockedCardDao,
    private val api: VendorAPI
) : CardRepository {

    override fun saveCardPayment(cardPayment: CardPayment): Completable {
        return Completable.fromCallable { cardPaymentDao.insert(convertToDb(cardPayment)) }
    }

    override fun sendCardPaymentToServer(cardPayment: CardPayment): Single<Int> {
        return api.postCardPayment(cardPayment.cardId, convertToApi(cardPayment)).map { response ->
            response.code()
        }
    }

    override fun getCardPayments(): Single<List<CardPayment>> {
        return cardPaymentDao.getAll().map { list ->
            list.map {
                convert(it)
            }
        }
    }

    override fun getBlockedCardsFromServer(): Single<Pair<Int, List<String>>> {
        return api.getBlockedCards().map { response ->
            Pair(response.code(), response.body() ?: listOf())
        }
    }

    override fun getBlockedCards(): Single<List<String>> {
        return blockedCardDao.getAll().map {
            it.map { it.id }
        }
    }

    override fun getBlockedCard(id: String): Maybe<String> {
        return blockedCardDao.getBlockedCard(id).map {
            it.id
        }
    }

    override fun saveBlockedCard(cardId: String): Completable {
        return Completable.fromCallable {
            blockedCardDao.insert(BlockedCardDbEntity(cardId))
        }
    }

    override fun deleteAllCardPayments(): Completable {
        return Completable.fromCallable { cardPaymentDao.deleteAll() }
    }

    override fun deleteAllBlockedCards(): Completable {
        return Completable.fromCallable { blockedCardDao.deleteAll() }
    }

    private fun convertToApi(cardPayment: CardPayment): CardPaymentApiEntity {
        return CardPaymentApiEntity().apply {
            productId = cardPayment.productId
            value = cardPayment.value
            createdAt = cardPayment.createdAt
        }
    }

    private fun convertToDb(cardPayment: CardPayment): CardPaymentDbEntity {
        return CardPaymentDbEntity().apply {
            cardId = cardPayment.cardId
            productId = cardPayment.productId
            value = cardPayment.value
            createdAt = cardPayment.createdAt
        }
    }

    private fun convert(cardPaymentDbEntity: CardPaymentDbEntity): CardPayment {
        return CardPayment().apply {
            cardId = cardPaymentDbEntity.cardId
            productId = cardPaymentDbEntity.productId
            value = cardPaymentDbEntity.value
            createdAt = cardPaymentDbEntity.createdAt
        }
    }
}
