package cz.quanti.android.vendor_app.repository.category

import cz.quanti.android.vendor_app.repository.category.dto.Category
import io.reactivex.Completable
import io.reactivex.Single

interface CategoryRepository {
    fun loadCategoriesFromServer(vendorId: Int): Single<Pair<Int, List<Category>>>
    fun getCategory(categoryId: Long): Category
    fun saveCategory(category: Category): Completable
    fun deleteCategories(): Completable
}
