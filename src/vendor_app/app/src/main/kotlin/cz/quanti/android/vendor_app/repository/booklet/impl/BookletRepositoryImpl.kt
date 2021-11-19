package cz.quanti.android.vendor_app.repository.booklet.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.booklet.BookletRepository
import cz.quanti.android.vendor_app.repository.booklet.dao.BookletDao
import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet
import cz.quanti.android.vendor_app.repository.booklet.dto.api.BookletApiEntity
import cz.quanti.android.vendor_app.repository.booklet.dto.api.BookletCodesBody
import cz.quanti.android.vendor_app.repository.booklet.dto.api.BookletsWithResponseCode
import cz.quanti.android.vendor_app.repository.booklet.dto.db.BookletDbEntity
import io.reactivex.Completable
import io.reactivex.Single

class BookletRepositoryImpl(
    private val bookletDao: BookletDao,
    private val api: VendorAPI
) : BookletRepository {

    override fun getAllDeactivatedBooklets(): Single<List<Booklet>> {
        return bookletDao.getAllDeactivated().map { list ->
            list.map { convert(it) }
        }
    }

    override fun getNewlyDeactivatedBooklets(): Single<List<Booklet>> {
        return bookletDao.getNewlyDeactivated().map { list ->
            list.map { convert(it) }
        }
    }

    override fun saveBooklet(booklet: Booklet): Completable {
        return Completable.fromCallable { bookletDao.insert(convert(booklet)) }
    }

    override fun loadProtectedBookletsFromServer(): Single<BookletsWithResponseCode> {
        return api.getProtectedBooklets().map { response ->
            var booklets = response.body()
            if (booklets == null) {
                booklets = listOf()
            }
            BookletsWithResponseCode(
                booklets = booklets.map {
                    convert(it).apply {
                        this.state = Booklet.STATE_PROTECTED
                    }
                },
                responseCode = response.code()
            )
        }
    }

    override fun loadDeactivatedBookletsFromServer(): Single<BookletsWithResponseCode> {
        return api.getDeactivatedBooklets().map { response ->
            var booklets = response.body()
            if (booklets == null) {
                booklets = listOf()
            }

            BookletsWithResponseCode(
                booklets = booklets.map {
                    convert(it).apply {
                        this.state = Booklet.STATE_DEACTIVATED
                    }
                },
                responseCode = response.code()
            )
        }
    }

    override fun deleteDeactivated(): Completable {
        return Completable.fromCallable { bookletDao.deleteDeactivated() }
    }

    override fun deleteProtected(): Completable {
        return Completable.fromCallable { bookletDao.deleteProtected() }
    }

    override fun deleteNewlyDeactivated(): Completable {
        return Completable.fromCallable { bookletDao.deleteNewlyDeactivated() }
    }

    override fun sendDeactivatedBookletsToServer(booklets: List<Booklet>): Single<Int> {
        return api.postBooklets(BookletCodesBody(booklets.map { it.code }))
            .map { response ->
                response.code()
            }
    }

    override fun getProtectedBooklets(): Single<List<Booklet>> {
        return bookletDao.getProtected().map { list ->
            list.map {
                convert(it)
            }
        }
    }

    override fun getNewlyDeactivatedCount(): Single<Int> {
        return bookletDao.getNewlyDeactivatedCount()
    }

    private fun convert(apiEntity: BookletApiEntity): Booklet {
        return Booklet().apply {
            this.code = apiEntity.code
            this.id = apiEntity.id
            this.password = apiEntity.password ?: ""
        }
    }

    private fun convert(booklet: Booklet): BookletDbEntity {
        return BookletDbEntity().apply {
            this.code = booklet.code
            this.id = booklet.id
            this.password = booklet.password
            this.state = booklet.state
        }
    }

    private fun convert(dbEntity: BookletDbEntity): Booklet {
        return Booklet().apply {
            this.code = dbEntity.code
            this.id = dbEntity.id
            this.state = dbEntity.state
            this.password = dbEntity.password
        }
    }
}
