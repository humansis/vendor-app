package cz.quanti.android.vendor_app.repository.voucher.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.product.dao.SelectedProductDao
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.product.dto.api.SelectedProductApiEntity
import cz.quanti.android.vendor_app.repository.product.dto.db.SelectedProductDbEntity
import cz.quanti.android.vendor_app.repository.voucher.VoucherRepository
import cz.quanti.android.vendor_app.repository.voucher.dao.BookletDao
import cz.quanti.android.vendor_app.repository.voucher.dao.VoucherDao
import cz.quanti.android.vendor_app.repository.voucher.dao.VoucherPurchaseDao
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.VoucherPurchase
import cz.quanti.android.vendor_app.repository.voucher.dto.api.BookletApiEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.api.BookletCodesBody
import cz.quanti.android.vendor_app.repository.voucher.dto.api.VoucherPurchaseApiEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.db.BookletDbEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.db.VoucherDbEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.db.VoucherPurchaseDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class VoucherRepositoryImpl(
    private val voucherDao: VoucherDao,
    private val bookletDao: BookletDao,
    private val voucherPurchaseDao: VoucherPurchaseDao,
    private val selectedProductDao: SelectedProductDao,
    private val api: VendorAPI
) : VoucherRepository {

    override fun getVoucherPurchases(): Single<List<VoucherPurchase>> {
        return voucherPurchaseDao.getAll().flatMap { purchases ->
            Observable.fromIterable(purchases.map { Pair(convert(it), it.dbId) })
                .flatMapSingle {
                    var purchase = it.first
                    val purchaseDbId = it.second
                    voucherDao.getVouchersForPurchase(purchaseDbId).flatMap {
                        purchase.vouchers.addAll(it.map { it.id })
                        selectedProductDao.getProductsForPurchase(purchaseDbId)
                            .flatMap { selectedProducts ->
                                purchase.products.addAll(selectedProducts.map { convert(it) })
                                Single.just(purchase)
                            }
                    }
                }.toList()
        }
    }

    override fun deleteAllVoucherPurchases(): Completable {
        return Completable.fromCallable { voucherPurchaseDao.deleteAll() }
    }

    override fun deleteAllSelectedProducts(): Completable {
        return Completable.fromCallable { selectedProductDao.deleteAll() }
    }

    override fun saveVoucherPurchase(purchase: VoucherPurchase): Single<Long> {
        return voucherPurchaseDao.insert(convertToDb(purchase))
    }

    override fun saveSelectedProduct(
        selectedProduct: SelectedProduct,
        purchaseDbId: Long
    ): Single<Long> {
        val product = convertToDb(selectedProduct).apply {
            purchaseId = purchaseDbId
        }
        return selectedProductDao.insert(product)
    }

    override fun saveVoucher(voucherId: Long, purchaseDbId: Long): Single<Long> {
        val voucher = VoucherDbEntity(id = voucherId, purchaseId = purchaseDbId)
        return voucherDao.insert(voucher)
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

    override fun deleteAllVouchers(): Completable {
        return Completable.fromCallable { voucherDao.deleteAll() }
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

    override fun sendVoucherPurchasesToServer(purchases: List<VoucherPurchase>): Single<Int> {
        return api.postVoucherPurchases(
            purchases.map { convertToApi(it) })
            .map { response -> response.code() }
    }

    override fun sendDeactivatedBookletsToServer(booklets: List<Booklet>): Single<Int> {
        return api.postBooklets(BookletCodesBody(booklets.map { it.code }))
            .map { response ->
            response.code()
        }
    }

    override fun getProtectedBooklets(): Single<List<Booklet>> {
        return bookletDao.getProtected().map { list ->
            list.map {
                convert(it)
            }
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

    private fun convertToApi(voucherPurchase: VoucherPurchase): VoucherPurchaseApiEntity {
        return VoucherPurchaseApiEntity().apply {
            products = voucherPurchase.products.map { convertToApi(it) }
            vouchers = voucherPurchase.vouchers
            vendorId = voucherPurchase.vendorId
            createdAt = voucherPurchase.createdAt
        }
    }

    private fun convertToApi(selectedProduct: SelectedProduct): SelectedProductApiEntity {
        return SelectedProductApiEntity().apply {
            id = selectedProduct.product.id
            quantity = selectedProduct.quantity
            value = selectedProduct.subTotal
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

    private fun convert(entity: VoucherPurchaseDbEntity): VoucherPurchase {
        return VoucherPurchase().apply {
            vendorId = entity.vendorId
            createdAt = entity.createdAt
        }
    }

    private fun convert(entity: SelectedProductDbEntity): SelectedProduct {
        return SelectedProduct().apply {
            product = Product(id = entity.productId)
            quantity = entity.quantity
            price = entity.value / entity.quantity
            subTotal = entity.value
        }
    }

    private fun convertToDb(selectedProduct: SelectedProduct): SelectedProductDbEntity {
        return SelectedProductDbEntity().apply {
            productId = selectedProduct.product.id
            quantity = selectedProduct.quantity
            value = selectedProduct.subTotal
        }
    }

    private fun convertToDb(voucherPurchase: VoucherPurchase): VoucherPurchaseDbEntity {
        return VoucherPurchaseDbEntity().apply {
            vendorId = voucherPurchase.vendorId
            createdAt = voucherPurchase.createdAt
        }
    }
}
