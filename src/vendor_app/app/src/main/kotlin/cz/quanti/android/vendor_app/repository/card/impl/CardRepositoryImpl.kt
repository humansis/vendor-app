package cz.quanti.android.vendor_app.repository.card.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.card.dao.BlockedCardDao
import cz.quanti.android.vendor_app.repository.card.dto.db.BlockedCardDbEntity
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

class CardRepositoryImpl(
    private val blockedCardDao: BlockedCardDao,
    private val api: VendorAPI
) : CardRepository {

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

    override fun deleteAllBlockedCards(): Completable {
        return Completable.fromCallable { blockedCardDao.deleteAll() }
    }
}
