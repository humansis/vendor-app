package cz.quanti.android.vendor_app.repository.category.impl

import cz.quanti.android.vendor_app.repository.category.CategoryFacade
import cz.quanti.android.vendor_app.repository.category.CategoryRepository
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import quanti.com.kotlinlog.Log

class CategoryFacadeImpl(
    private val categoryRepo: CategoryRepository
) : CategoryFacade {

    override fun syncWithServer(
        vendorId: Int
    ): Observable<SynchronizationSubject> {
        return Observable.just(SynchronizationSubject.CATEGORIES_DOWNLOAD)
            .concatWith(loadCategoriesFromServer(vendorId))
    }

    private fun loadCategoriesFromServer(
        vendorId: Int
    ): Completable {
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
            Log.d("Categories returned from server were empty.")
            Completable.complete()
        } else {
            categoryRepo.deleteCategories().andThen(
                Observable.fromIterable(categories).flatMapCompletable { category ->
                    categoryRepo.saveCategory(category)
                })
        }
    }
}
