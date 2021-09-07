package cz.quanti.android.vendor_app.repository.category

import cz.quanti.android.vendor_app.repository.category.dto.Category
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface CategoryRepository {
    fun getCategoriesFromServer(): Single<Pair<Int, List<Category>>>
    fun getCategories(): Observable<List<Category>>
    fun getCategory(category: Category): Category
    fun saveCategory(category: Category): Completable
    fun deleteCategories(): Completable
}
