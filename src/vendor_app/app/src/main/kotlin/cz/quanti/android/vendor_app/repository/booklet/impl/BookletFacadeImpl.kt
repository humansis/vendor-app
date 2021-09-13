package cz.quanti.android.vendor_app.repository.booklet.impl

import cz.quanti.android.vendor_app.repository.booklet.BookletFacade
import cz.quanti.android.vendor_app.repository.booklet.BookletRepository
import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class BookletFacadeImpl(
    private val bookletRepo: BookletRepository
) : BookletFacade {

    override fun getAllDeactivatedBooklets(): Single<List<Booklet>> {
        return bookletRepo.getAllDeactivatedBooklets()
    }

    override fun deactivate(booklet: String): Completable {
        return bookletRepo.saveBooklet(Booklet().apply {
            this.code = booklet
            this.state = Booklet.STATE_NEWLY_DEACTIVATED
        })
    }

    override fun getProtectedBooklets(): Single<List<Booklet>> {
        return bookletRepo.getProtectedBooklets()
    }

    override fun syncWithServer(): Completable {
        return sendDataToServer()
            .andThen(loadDataFromServer())
    }

    override fun isSyncNeeded(): Single<Boolean> {
        return bookletRepo.getNewlyDeactivatedCount().map { it > 0 }
    }

    private fun sendDataToServer(): Completable {
        return sendDeactivatedBooklets()
    }

    private fun loadDataFromServer(): Completable {
        return reloadDeactivatedBookletsFromServer()
            .andThen(reloadProtectedBookletsFromServer())
    }

    private fun sendDeactivatedBooklets(): Completable {
        return bookletRepo.getNewlyDeactivatedBooklets().flatMapCompletable { booklets ->
            if (booklets.isNotEmpty()) {
                bookletRepo.sendDeactivatedBookletsToServer(booklets)
                    .flatMapCompletable { responseCode ->
                        if (isPositiveResponseHttpCode(responseCode)) {
                            Completable.complete()
                        } else {
                            throw VendorAppException("Could not send booklets to server").apply {
                                apiError = true
                                apiResponseCode = responseCode
                            }
                        }
                    }
            } else {
                Completable.complete()
            }
        }
    }

    private fun reloadDeactivatedBookletsFromServer(): Completable {
        return bookletRepo.loadDeactivatedBookletsFromServer().flatMapCompletable { response ->
            val responseCode = response.responseCode
            if (isPositiveResponseHttpCode(responseCode)) {
                val booklets = response.booklets
                bookletRepo.deleteDeactivated()
                    .andThen(Observable.fromIterable(booklets).flatMapCompletable { booklet ->
                        booklet.state = Booklet.STATE_DEACTIVATED
                        bookletRepo.saveBooklet(booklet)
                    })
            } else {
                throw VendorAppException("Could not load deactivated booklets").apply {
                    this.apiResponseCode = responseCode
                    this.apiError = true
                }
            }
        }
    }

    private fun reloadProtectedBookletsFromServer(): Completable {
        return bookletRepo.loadProtectedBookletsFromServer().flatMapCompletable { response ->
            val responseCode = response.responseCode

            if (isPositiveResponseHttpCode(responseCode)) {
                val booklets = response.booklets
                bookletRepo.deleteProtected()
                    .andThen(Observable.fromIterable(booklets).flatMapCompletable { booklet ->
                        booklet.state = Booklet.STATE_PROTECTED
                        bookletRepo.saveBooklet(booklet)
                    })
            } else {
                throw VendorAppException("Could not load protected booklets").apply {
                    this.apiResponseCode = responseCode
                    this.apiError = true
                }
            }
        }
    }
}
