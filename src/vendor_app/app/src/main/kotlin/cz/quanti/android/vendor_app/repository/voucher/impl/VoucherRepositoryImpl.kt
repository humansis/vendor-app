package cz.quanti.android.vendor_app.repository.voucher.impl

import cz.quanti.android.vendor_app.repository.utils.wrapper.ProductIdListWrapper
import cz.quanti.android.vendor_app.repository.voucher.VoucherRepository
import cz.quanti.android.vendor_app.repository.voucher.dao.BookletDao
import cz.quanti.android.vendor_app.repository.voucher.dao.VoucherDao
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.repository.voucher.dto.db.BookletDbEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.db.VoucherDbEntity
import io.reactivex.Completable
import io.reactivex.Single

class VoucherRepositoryImpl(
    private val voucherDao: VoucherDao,
    private val bookletDao: BookletDao
) : VoucherRepository {

    override fun saveVoucher(voucher: Voucher): Completable {
        return Completable.fromCallable { voucherDao.insert(convert(voucher)) }
    }

    override fun getDeactivatedBooklets(): Single<List<Booklet>> {
        return bookletDao.getDeactivated().map { list ->
            list.map { convert(it) }
        }
    }

    private fun convert(dbEntity: BookletDbEntity): Booklet {
        return Booklet().apply {
            this.code = dbEntity.code
            this.id = dbEntity.id
            this.state = dbEntity.state
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
