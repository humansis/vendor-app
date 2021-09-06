package cz.quanti.android.vendor_app.repository.category.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.category.CategoryRepository
import cz.quanti.android.vendor_app.repository.category.dao.CategoryDao
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.category.dto.api.CategoryApiEntity
import cz.quanti.android.vendor_app.repository.category.dto.db.CategoryDbEntity
import io.reactivex.Observable
import io.reactivex.Single
import quanti.com.kotlinlog.android.extractFieldName

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

    private fun convert(category: CategoryApiEntity): Category {
        return Category(
            id = category.id,
            name = category.name,
            type = category.type,
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
