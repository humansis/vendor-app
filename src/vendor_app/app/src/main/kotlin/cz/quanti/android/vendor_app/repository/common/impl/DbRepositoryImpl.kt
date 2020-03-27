package cz.quanti.android.vendor_app.repository.common.impl

import cz.quanti.android.vendor_app.repository.common.DbRepository
import cz.quanti.android.vendor_app.repository.product.dao.ProductDao
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import cz.quanti.android.vendor_app.repository.utils.wrapper.ProductIdListWrapper
import cz.quanti.android.vendor_app.repository.voucher.dao.VoucherDao
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.repository.voucher.dto.db.VoucherDbEntity
import io.reactivex.Completable
import io.reactivex.Single

class DbRepositoryImpl(private val productDao: ProductDao, private val voucherDao: VoucherDao) :
    DbRepository {
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

    override fun saveVoucher(voucher: Voucher): Completable {
        return Completable.fromCallable { voucherDao.insert(convert(voucher)) }
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

    private fun convert(dbEntity: VoucherDbEntity): Voucher {
        return Voucher().apply {
            id = dbEntity.id
            qrCode = dbEntity.qrCode
            vendorId = dbEntity.vendorId
            productIds = dbEntity.productIds.products.toTypedArray()
            price = dbEntity.price
            currency = dbEntity.currency
            value = dbEntity.value
            booklet = dbEntity.booklet
            usedAt = dbEntity.usedAt
        }
    }

    private fun convert(entity: Voucher): VoucherDbEntity {
        return VoucherDbEntity()
            .apply {
            id = entity.id
            qrCode = entity.qrCode
            vendorId = entity.vendorId
                productIds =
                    ProductIdListWrapper(
                        entity.productIds.toList()
                    )
            price = entity.price
            currency = entity.currency
            value = entity.value
            booklet = entity.booklet
            usedAt = entity.usedAt
        }
    }
}
