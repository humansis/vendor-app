package cz.quanti.android.vendor_app.repository.card.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.card.dao.BlockedCardDao
import cz.quanti.android.vendor_app.repository.card.dto.db.BlockedCardDbEntity
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

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
        return blockedCardDao.getAll().map { list ->
            list.map { it.id.uppercase(Locale.US) }
        }
    }

    override fun isBlockedCard(id: String?): Single<Boolean> {
        return Single.fromCallable {
            if (id != null) {
                blockedCardDao.isBlockedCard(id)
            } else false
        }
    }

    override fun saveBlockedCard(cardId: String): Completable {
        return Completable.fromCallable {
            blockedCardDao.insert(BlockedCardDbEntity(cardId.uppercase(Locale.US)))
        }
    }

    override fun deleteAllBlockedCards(): Completable {
        return Completable.fromCallable { blockedCardDao.deleteAll() }
    }
}
