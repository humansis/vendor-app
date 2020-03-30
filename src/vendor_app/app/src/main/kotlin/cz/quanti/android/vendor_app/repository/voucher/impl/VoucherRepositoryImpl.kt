package cz.quanti.android.vendor_app.repository.voucher.impl

import cz.quanti.android.vendor_app.repository.utils.wrapper.ProductIdListWrapper
import cz.quanti.android.vendor_app.repository.voucher.VoucherRepository
import cz.quanti.android.vendor_app.repository.voucher.dao.VoucherDao
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.repository.voucher.dto.db.VoucherDbEntity
import io.reactivex.Completable

class VoucherRepositoryImpl(private val voucherDao: VoucherDao) : VoucherRepository {

    override fun saveVoucher(voucher: Voucher): Completable {
        return Completable.fromCallable { voucherDao.insert(convert(voucher)) }
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
