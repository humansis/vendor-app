package cz.quanti.android.vendor_app.repository.category.impl

import cz.quanti.android.vendor_app.repository.category.CategoryFacade
import cz.quanti.android.vendor_app.repository.category.dto.Category
import io.reactivex.Observable

class CategoryFacadeImpl(): CategoryFacade {
    override fun getCategories(): Observable<List<Category>> {
        TODO("Not yet implemented")
    }
}
