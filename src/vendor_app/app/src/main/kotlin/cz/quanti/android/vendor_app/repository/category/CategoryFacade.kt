package cz.quanti.android.vendor_app.repository.category

import cz.quanti.android.vendor_app.repository.category.dto.Category
import io.reactivex.Observable

interface CategoryFacade {
    fun getCategories(): Observable<List<Category>>
}
