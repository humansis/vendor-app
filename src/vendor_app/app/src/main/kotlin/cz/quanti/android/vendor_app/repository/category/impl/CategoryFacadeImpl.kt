package cz.quanti.android.vendor_app.repository.category.impl

import cz.quanti.android.vendor_app.repository.category.CategoryFacade
import cz.quanti.android.vendor_app.repository.category.CategoryRepository
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable

class CategoryFacadeImpl(
    private val categoryRepo: CategoryRepository
): CategoryFacade {

    override fun syncWithServer(vendorId: Int): Completable {
        return loadCategoriesFromServer(vendorId)
    }

    private fun loadCategoriesFromServer(vendorId: Int): Completable {
        return categoryRepo.loadCategoriesFromServer(vendorId).flatMapCompletable { response ->
            val responseCode = response.first
            val categories = response.second.toMutableList()
            if (isPositiveResponseHttpCode(responseCode)) {
                actualizeDatabase(categories)
            } else {
                Completable.error(
                    VendorAppException(
                        "Could not get categories from server."
                    ).apply {
                        apiError = true
                        apiResponseCode = responseCode
                    })
            }
        }
    }

    private fun actualizeDatabase(categories: List<Category>?): Completable {
        return if (categories == null) {
            throw VendorAppException("Categories returned from server were empty.")
        } else {
            categoryRepo.deleteCategories().andThen(
                Observable.fromIterable(categories).flatMapCompletable { category ->
                    categoryRepo.saveCategory(category)
                })
        }
    }

    override fun getCategories(): Observable<List<Category>> {
        return categoryRepo.getCategories()
    }
}
