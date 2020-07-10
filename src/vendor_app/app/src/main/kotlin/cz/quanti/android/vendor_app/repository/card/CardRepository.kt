package cz.quanti.android.vendor_app.repository.card

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface CardRepository {

    fun getBlockedCardsFromServer(): Single<Pair<Int, List<String>>>

    fun getBlockedCards(): Single<List<String>>

    fun getBlockedCard(id: String): Maybe<String>

    fun saveBlockedCard(cardId: String): Completable

    fun deleteAllBlockedCards(): Completable
}
