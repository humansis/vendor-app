package cz.quanti.android.vendor_app.repository.category

import cz.quanti.android.vendor_app.repository.category.dto.Category
import io.reactivex.Completable
import io.reactivex.Observable

interface CategoryFacade {

    fun syncWithServer(vendorId: Int): Completable

    fun getCategories(): Observable<List<Category>>
}
