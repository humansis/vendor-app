package cz.quanti.android.vendor_app.repository.category.impl

import cz.quanti.android.vendor_app.repository.category.CategoryFacade
import cz.quanti.android.vendor_app.repository.category.CategoryRepository
import cz.quanti.android.vendor_app.repository.category.dto.Category
import io.reactivex.Observable

class CategoryFacadeImpl(
    private val categoryRepo: CategoryRepository
): CategoryFacade {
    override fun getCategories(): Observable<List<Category>> {
        return categoryRepo.getCategories()
    }
}
