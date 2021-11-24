package cz.quanti.android.vendor_app.repository.product.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.category.CategoryRepository
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.product.ProductRepository
import cz.quanti.android.vendor_app.repository.product.dao.ProductDao
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.api.ProductApiEntity
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class ProductRepositoryImpl(
    private val categoryRepository: CategoryRepository,
    private val productDao: ProductDao,
    private val api: VendorAPI
) :
    ProductRepository {

    override fun loadProductsFromServer(vendor: Vendor): Single<Pair<Int, List<Product>>> {
        return api.getProducts(vendor.id.toInt()).map { response ->
            var products = response.body()?.data
            if (products == null) {
                products = listOf()
            }
            Pair(response.code(), products.map { productApiEntity ->
                convert(productApiEntity)
            })
        }
    }

    override fun getProducts(): Observable<List<Product>> {
        return productDao.getAll().map { products ->
            products.map {
                convert(it)
            }
        }
    }

    override fun deleteProducts(): Completable {
        return productDao.deleteAll()
    }

    override fun saveProduct(product: Product): Completable {
        return productDao.insert(convert(product))
    }

    private fun convert(dbEntity: ProductDbEntity): Product {
        return Product().apply {
            this.id = dbEntity.id
            this.name = dbEntity.name
            this.image = dbEntity.image
            this.unit = dbEntity.unit
            this.category = categoryRepository.getCategory(dbEntity.categoryId)
            this.unitPrice = dbEntity.unitPrice
            this.currency = dbEntity.currency
        }
    }

    private fun convert(product: Product): ProductDbEntity {
        return ProductDbEntity()
            .apply {
                this.id = product.id
                this.name = product.name
                this.image = product.image
                this.unit = product.unit
                this.categoryId = product.category.id
                this.unitPrice = product.unitPrice
                this.currency = product.currency
            }
    }

    private fun convert(apiEntity: ProductApiEntity): Product {
        return Product().apply {
            this.id = apiEntity.id
            this.name = apiEntity.name
            this.image = apiEntity.image
            this.unit = apiEntity.unit ?: ""
            this.category = categoryRepository.getCategory(apiEntity.productCategoryId)
            this.unitPrice = apiEntity.unitPrice
            this.currency = apiEntity.currency
        }
    }
}
