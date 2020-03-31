package cz.quanti.android.vendor_app.repository.voucher.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.utils.wrapper.ProductIdListWrapper
import cz.quanti.android.vendor_app.repository.voucher.VoucherRepository
import cz.quanti.android.vendor_app.repository.voucher.dao.BookletDao
import cz.quanti.android.vendor_app.repository.voucher.dao.VoucherDao
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.repository.voucher.dto.api.BookletApiEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.api.BookletCodesBody
import cz.quanti.android.vendor_app.repository.voucher.dto.api.VoucherApiEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.db.BookletDbEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.db.VoucherDbEntity
import io.reactivex.Completable
import io.reactivex.Single

class VoucherRepositoryImpl(
    private val voucherDao: VoucherDao,
    private val bookletDao: BookletDao,
    private val api: VendorAPI
) : VoucherRepository {
    override fun getVouchers(): Single<List<Voucher>> {
        return voucherDao.getAll().map { list ->
            list.map {
                convert(it)
            }
        }
    }

    override fun saveVoucher(voucher: Voucher): Completable {
        return Completable.fromCallable { voucherDao.insert(convert(voucher)) }
    }

    override fun getAllDeactivatedBooklets(): Single<List<Booklet>> {
        return bookletDao.getAllDeactivated().map { list ->
            list.map { convert(it) }
        }
    }

    override fun getNewlyDeactivatedBooklets(): Single<List<Booklet>> {
        return bookletDao.getNewlyDeactivated().map { list ->
            list.map { convert(it) }
        }
    }

    override fun saveBooklet(booklet: Booklet): Completable {
        return Completable.fromCallable { bookletDao.insert(convert(booklet)) }
    }

    override fun getProtectedBookletsFromServer(): Single<Pair<Int, List<Booklet>>> {
        return api.getProtectedBooklets().map { response ->
            var booklets = response.body()
            if (booklets == null) {
                booklets = listOf()
            }
            Pair(response.code(), booklets.map {
                convert(it).apply {
                    this.state = Booklet.STATE_PROTECTED
                }
            })
        }
    }

    override fun getDeactivatedBookletsFromServer(): Single<Pair<Int, List<Booklet>>> {
        return api.getDeactivatedBooklets().map { response ->
            var booklets = response.body()
            if (booklets == null) {
                booklets = listOf()
            }
            Pair(response.code(), booklets.map {
                convert(it).apply {
                    this.state = Booklet.STATE_DEACTIVATED
                }
            })
        }
    }

    override fun deleteDeactivated(): Completable {
        return Completable.fromCallable { bookletDao.deleteDeactivated() }
    }

    override fun deleteProtected(): Completable {
        return Completable.fromCallable { bookletDao.deleteProtected() }
    }

    override fun deleteNewlyDeactivated(): Completable {
        return Completable.fromCallable { bookletDao.deleteNewlyDeactivated() }
    }

    override fun sendVouchersToServer(vouchers: List<Voucher>): Single<Int> {
        return api.postVouchers(vouchers.map { convertToApi(it) }).map { response ->
            response.code()
        }
    }

    override fun sendDeactivatedBookletsToServer(booklets: List<Booklet>): Single<Int> {
        return api.postBooklets(BookletCodesBody(booklets.map { it.code })).map { response ->
            response.code()
        }
    }

    private fun convert(apiEntity: BookletApiEntity): Booklet {
        return Booklet().apply {
            this.code = apiEntity.code
            this.id = apiEntity.id
            this.password = apiEntity.password
        }
    }

    private fun convertToApi(booklet: Booklet): BookletApiEntity {
        return BookletApiEntity().apply {
            this.code = booklet.code
            this.id = booklet.id
            this.password = booklet.password
        }
    }

    private fun convert(booklet: Booklet): BookletDbEntity {
        return BookletDbEntity().apply {
            this.code = booklet.code
            this.id = booklet.id
            this.password = booklet.password
            this.state = booklet.state
        }
    }

    private fun convert(dbEntity: BookletDbEntity): Booklet {
        return Booklet().apply {
            this.code = dbEntity.code
            this.id = dbEntity.id
            this.state = dbEntity.state
            this.password = dbEntity.password
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

    private fun convertToApi(entity: Voucher): VoucherApiEntity {
        return VoucherApiEntity()
            .apply {
                id = entity.id
                qrCode = entity.qrCode
                vendorId = entity.vendorId
                productIds = entity.productIds.toList().toTypedArray()
                price = entity.price
                currency = entity.currency
                value = entity.value
                booklet = entity.booklet
                usedAt = entity.usedAt
            }
    }
}
