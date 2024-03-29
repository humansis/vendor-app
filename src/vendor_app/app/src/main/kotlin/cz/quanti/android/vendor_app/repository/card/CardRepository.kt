package cz.quanti.android.vendor_app.repository.card

import io.reactivex.Completable
import io.reactivex.Single

interface CardRepository {

    fun loadBlockedCardsFromServer(): Single<Pair<Int, List<String>>>

    fun getBlockedCards(): Single<List<String>>

    fun isBlockedCard(id: String?): Single<Boolean>

    fun saveBlockedCard(cardId: String): Completable

    fun deleteAllBlockedCards(): Completable
}
