package cz.quanti.android.vendor_app.repository.product

import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

interface ProductFacade {

    fun getProducts(): Observable<List<Product>>

    fun syncWithServer(
        syncSubjectReplaySubject: ReplaySubject<SynchronizationSubject>,
        vendorId: Int
    ): Completable

    fun deleteProducts(): Completable
}
