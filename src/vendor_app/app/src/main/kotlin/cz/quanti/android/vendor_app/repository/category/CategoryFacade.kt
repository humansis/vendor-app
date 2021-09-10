package cz.quanti.android.vendor_app.repository.category

import io.reactivex.Completable

interface CategoryFacade {

    fun syncWithServer(vendorId: Int): Completable
}
