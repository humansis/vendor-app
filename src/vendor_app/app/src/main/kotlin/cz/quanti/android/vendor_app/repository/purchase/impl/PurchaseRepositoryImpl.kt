package cz.quanti.android.vendor_app.repository.purchase.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.PurchaseRepository
import cz.quanti.android.vendor_app.repository.purchase.dao.CardPurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dao.PurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dao.SelectedProductDao
import cz.quanti.android.vendor_app.repository.purchase.dao.VoucherPurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.purchase.dto.api.*
import cz.quanti.android.vendor_app.repository.purchase.dto.db.CardPurchaseDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.PurchaseDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.SelectedProductDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.VoucherPurchaseDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import quanti.com.kotlinlog.Log

class PurchaseRepositoryImpl(
    private val purchaseDao: PurchaseDao,
    private val cardPurchaseDao: CardPurchaseDao,
    private val voucherPurchaseDao: VoucherPurchaseDao,
    private val selectedProductDao: SelectedProductDao,
    private val api: VendorAPI
) : PurchaseRepository {

    override fun savePurchase(purchase: Purchase): Completable {
        return Single.fromCallable { purchaseDao.insert(convertToDb(purchase)) }
            .flatMapCompletable { purchaseId ->
                saveSelectedProducts(purchaseId, purchase.products).andThen(
                    saveToDb(
                        purchase,
                        purchaseId
                    )
                )
            }
    }

    private fun saveToDb(purchase: Purchase, id: Long): Completable {
        if (purchase.smartcard != null) {
            return Completable.fromCallable {
                cardPurchaseDao.insert(
                    CardPurchaseDbEntity(
                        purchaseId = id,
                        card = purchase.smartcard!!
                    )
                )
            }
        } else {
            return Observable.fromIterable(purchase.vouchers.toList())
                .flatMapCompletable { voucher ->
                    Completable.fromCallable {
                        voucherPurchaseDao.insert(
                            VoucherPurchaseDbEntity(
                                purchaseId = id,
                                voucher = voucher
                            )
                        )
                    }
                }
        }
    }

    override fun sendCardPurchaseToServer(purchase: Purchase): Single<Int> {
        return if (purchase.smartcard != null) {
            api.postCardPurchase(purchase.smartcard!!, convertToCardApi(purchase)).map { response ->
                response.code()
            }
        } else {
            Single.just(200)
        }
    }

    override fun sendVoucherPurchasesToServer(purchases: List<Purchase>): Single<Int> {

        val voucherPurchases = purchases.map { convertToVoucherApi(it) }

        return if (voucherPurchases.isNotEmpty()) {
            api.postVoucherPurchases(voucherPurchases).map { response ->
                Log.d(
                    TAG,
                    "Received code ${response.code()} when trying to sync voucher purchases"
                )
                response.code()
            }
        } else {
            Single.just(200)
        }
    }

    override fun getAllPurchases(): Single<List<Purchase>> {
        return purchaseDao.getAll().flatMap { purchasesDb ->
            Observable.fromIterable(purchasesDb)
                .flatMapSingle { purchaseDb ->
                    selectedProductDao.getProductsForPurchase(purchaseDb.dbId)
                        .flatMap { productsDb ->
                            cardPurchaseDao.getCardForPurchase(purchaseDb.dbId)
                                .defaultIfEmpty(CardPurchaseDbEntity(card = null))
                                .toSingle()
                                .flatMap { cardPurchaseDb ->
                                    voucherPurchaseDao.getVouchersForPurchase(purchaseDb.dbId)
                                        .defaultIfEmpty(listOf())
                                        .flatMapSingle { voucherPurchasesDb ->
                                            val purchase = Purchase(
                                                smartcard = cardPurchaseDb.card,
                                                createdAt = purchaseDb.createdAt,
                                                dbId = purchaseDb.dbId
                                            )
                                            purchase.products.addAll(productsDb.map { convert(it) })
                                            purchase.vouchers.addAll(voucherPurchasesDb.map { it.voucher })
                                            purchase.vendorId = purchaseDb.vendorId
                                            Single.just(purchase)
                                        }
                                }
                        }
                }.toList()
        }
    }

    override fun getInvoices(vendorId: Int): Single<Pair<Int, List<InvoiceApiEntity>>> {
        return api.getInvoices(vendorId).map { response ->
            response.body()?.let { Pair(response.code(), it.data) }
        }
    }

    override fun getTransactions(vendorId: Int): Single<Pair<Int, List<TransactionsApiEntity>>> {
        return api.getTransactions(vendorId).map { response ->
            response.body()?.let { Pair(response.code(), it.data) }
        }
    }

    override fun getPurchasesById(purchaseIds: List<Int>): Single<Pair<Int, List<PurchaseApiEntity>>> {
        return api.getPurchasesById(purchaseIds).map { response ->
            response.body()?.let { Pair(response.code(), it.data) }
        }
    }

    override fun deleteAllPurchases(): Completable {
        return Completable.fromCallable {
            purchaseDao.deleteAll()
            voucherPurchaseDao.deleteAll()
            cardPurchaseDao.deleteAll()
            selectedProductDao.deleteAll()
        }
    }

    override fun deletePurchase(purchase: Purchase): Completable {
        return if (purchase.vouchers.isNotEmpty()) {
            deleteVoucherPurchase(purchase)
        } else {
            deleteCardPurchase(purchase)
        }
    }

    override fun deleteCardPurchase(purchase: Purchase): Completable {
        return Completable.fromCallable { cardPurchaseDao.deleteCardForPurchase(purchase.dbId) }
    }

    override fun deleteVoucherPurchase(purchase: Purchase): Completable {
        return Completable.fromCallable { voucherPurchaseDao.deleteVoucherForPurchase(purchase.dbId) }
    }

    override fun deleteAllVoucherPurchases(): Completable {
        return Completable.fromCallable{ voucherPurchaseDao.deleteAll() }
    }

    override fun getPurchasesCount(): Single<Int> {
        return purchaseDao.getCount()
    }

    private fun saveSelectedProducts(
        purchaseId: Long,
        products: List<SelectedProduct>
    ): Completable {
        return Observable.fromIterable(products).flatMapCompletable { selectedProduct ->
            Completable.fromCallable {
                selectedProductDao.insert(convertToDb(selectedProduct, purchaseId))
            }
        }
    }

    private fun convertToDb(
        selectedProduct: SelectedProduct,
        purchaseId: Long
    ): SelectedProductDbEntity {
        return SelectedProductDbEntity(
            productId = selectedProduct.product.id,
            value = selectedProduct.price,
            purchaseId = purchaseId
        )
    }

    private fun convert(selectedProductDbEntity: SelectedProductDbEntity): SelectedProduct {
        return SelectedProduct(
            price = selectedProductDbEntity.value,
            product = Product(id = selectedProductDbEntity.productId)
        )
    }

    private fun convertToApi(selectedProduct: SelectedProduct): SelectedProductApiEntity {
        return SelectedProductApiEntity(
            id = selectedProduct.product.id,
            value = selectedProduct.price
        )
    }

    private fun convertToDb(purchase: Purchase): PurchaseDbEntity {
        return PurchaseDbEntity(
            createdAt = purchase.createdAt,
            vendorId = purchase.vendorId,
            dbId = purchase.dbId
        )
    }

    private fun convertToCardApi(purchase: Purchase): CardPurchaseApiEntity {
        return CardPurchaseApiEntity(
            products = purchase.products.map { convertToApi(it) },
            createdAt = purchase.createdAt,
            vendorId = purchase.vendorId
        )
    }

    private fun convertToVoucherApi(purchase: Purchase): VoucherPurchaseApiEntity {
        return VoucherPurchaseApiEntity(
            products = purchase.products.map { convertToApi(it) },
            vouchers = purchase.vouchers,
            createdAt = purchase.createdAt,
            vendorId = purchase.vendorId
        )
    }

    companion object {
        private val TAG = PurchaseRepositoryImpl::class.java.simpleName
    }
}

