package cz.quanti.android.vendor_app.repository.booklet

import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet
import cz.quanti.android.vendor_app.repository.booklet.dto.api.BookletsWithResponseCode
import io.reactivex.Completable
import io.reactivex.Single

interface BookletRepository {

    fun getAllDeactivatedBooklets(): Single<List<Booklet>>

    fun saveBooklet(booklet: Booklet): Completable

    fun loadDeactivatedBookletsFromServer(): Single<BookletsWithResponseCode>

    fun loadProtectedBookletsFromServer(): Single<BookletsWithResponseCode>

    fun getNewlyDeactivatedBooklets(): Single<List<Booklet>>

    fun deleteDeactivated(): Completable

    fun deleteProtected(): Completable

    fun deleteNewlyDeactivated(): Completable

    fun sendDeactivatedBookletsToServer(booklets: List<Booklet>): Single<Int>

    fun getProtectedBooklets(): Single<List<Booklet>>

    fun getNewlyDeactivatedCount(): Single<Int>
}
