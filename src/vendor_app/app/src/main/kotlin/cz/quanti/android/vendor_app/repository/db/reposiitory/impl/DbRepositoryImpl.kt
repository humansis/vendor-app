package cz.quanti.android.vendor_app.repository.db.reposiitory.impl

import cz.quanti.android.vendor_app.repository.db.dao.ProductDao
import cz.quanti.android.vendor_app.repository.db.entity.ProductDbEntity
import cz.quanti.android.vendor_app.repository.db.reposiitory.DbRepository
import cz.quanti.android.vendor_app.repository.entity.Product
import io.reactivex.Completable
import io.reactivex.Single

class DbRepositoryImpl(private val productDao: ProductDao) : DbRepository {
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
        return ProductDbEntity().apply {
            this.id = entity.id
            this.name = entity.name
            this.image = entity.image
            this.unit = entity.unit
        }
    }
}
