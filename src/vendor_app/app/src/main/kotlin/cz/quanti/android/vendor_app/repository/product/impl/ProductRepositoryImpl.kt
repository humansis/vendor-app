package cz.quanti.android.vendor_app.repository.product.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.product.ProductRepository
import cz.quanti.android.vendor_app.repository.product.dao.ProductDao
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.api.ProductApiEntity
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import io.reactivex.Completable
import io.reactivex.Single

class ProductRepositoryImpl(private val productDao: ProductDao, private val api: VendorAPI) :
    ProductRepository {

    override fun getProductsFromServer(): Single<Pair<Int, List<Product>>> {
        return api.getProducts().map { response ->
            var products = response.body()
            if (products == null) {
                products = listOf()
            }
            Pair(response.code(), products.map { productApiEntity ->
                convert(productApiEntity)
            })
        }
    }

    override fun getProducts(): Single<List<Product>> {
        return productDao.getAll().map { products ->
            products.map {
                convert(it)
            }
        }
    }

    override fun deleteProducts(): Completable {
        return Completable.fromCallable { productDao.deleteAll() }
    }

    override fun saveProduct(product: Product): Completable {
        return Completable.fromCallable { productDao.insert(convert(product)) }
    }

    private fun convert(dbEntity: ProductDbEntity): Product {
        return Product().apply {
            this.id = dbEntity.id
            this.name = dbEntity.name
            this.image = dbEntity.image
            this.unit = dbEntity.unit
        }
    }

    private fun convert(entity: Product): ProductDbEntity {
        return ProductDbEntity()
            .apply {
                this.id = entity.id
                this.name = entity.name
                this.image = entity.image
                this.unit = entity.unit
            }
    }

    private fun convert(apiEntity: ProductApiEntity): Product {
        return Product().apply {
            this.id = apiEntity.id
            this.name = apiEntity.name
            this.image = apiEntity.image
            this.unit = apiEntity.unit
        }
    }
}
