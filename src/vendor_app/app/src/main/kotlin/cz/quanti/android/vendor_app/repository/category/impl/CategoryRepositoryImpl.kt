package cz.quanti.android.vendor_app.repository.category.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.category.CategoryRepository
import cz.quanti.android.vendor_app.repository.category.dao.CategoryDao
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.category.dto.api.CategoryApiEntity
import cz.quanti.android.vendor_app.repository.category.dto.db.CategoryDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val api: VendorAPI
): CategoryRepository {

    override fun getCategoriesFromServer(): Single<Pair<Int, List<Category>>> {
        return api.getCategories().map { response ->
            var categories = response.body()
            if (categories == null) {
                categories = listOf()
            }
            Pair(response.code(), categories.map { categoryApiEntity ->
                convert(categoryApiEntity)
            })
        }
    }

    override fun getCategories(): Observable<List<Category>> {
        return categoryDao.getAll().map { categories ->
            categories.map {
                convert(it)
            }
        }
    }

    override fun getCategory(category: Category): Category {
        return convert(categoryDao.getCategoryById(category.id))
    }

    override fun saveCategory(category: Category): Completable {
        return Completable.fromCallable { categoryDao.insert(convert(category)) }
    }

    override fun deleteCategories(): Completable {
        return Completable.fromCallable { categoryDao.deleteAll() }
    }

    private fun convert(category: CategoryApiEntity): Category {
        return Category(
            id = category.id,
            name = category.name,
            type = CategoryType.valueOf(category.type),
            image = category.image
        )
    }

    private fun convert(category: Category): CategoryDbEntity {
        return CategoryDbEntity(
            id = category.id,
            name = category.name,
            type = category.type.name,
            image = category.image
        )
    }

    private fun convert(category: CategoryDbEntity): Category {
        return Category(
            id = category.id,
            name = category.name,
            type = CategoryType.valueOf(category.type),
            image = category.image
        )
    }
}
