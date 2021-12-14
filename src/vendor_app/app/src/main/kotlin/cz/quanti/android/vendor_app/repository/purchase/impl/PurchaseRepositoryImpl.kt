package cz.quanti.android.vendor_app.repository.purchase.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.category.CategoryRepository
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.product.dao.ProductDao
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import cz.quanti.android.vendor_app.repository.purchase.PurchaseRepository
import cz.quanti.android.vendor_app.repository.purchase.dao.CardPurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dao.PurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dao.PurchasedProductDao
import cz.quanti.android.vendor_app.repository.purchase.dao.SelectedProductDao
import cz.quanti.android.vendor_app.repository.purchase.dao.VoucherPurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.PurchasedProduct
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.purchase.dto.api.CardPurchaseApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.PurchasedProductApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.VoucherPurchaseApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.CardPurchaseDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.PurchaseDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.PurchasedProductDbEntity
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
    private val categoryRepo: CategoryRepository,
    private val productDao: ProductDao,
    private val purchasedProductDao: PurchasedProductDao,
    private val selectedProductDao: SelectedProductDao,
    private val api: VendorAPI
) : PurchaseRepository {

    override fun savePurchase(purchase: Purchase): Completable {
        return Single.fromCallable { purchaseDao.insert(convertToDb(purchase)) }
            .flatMapCompletable { purchaseId ->
                savePurchasedProducts(purchaseId, purchase.products).andThen(
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
                        card = purchase.smartcard,
                        beneficiaryId = purchase.beneficiaryId,
                        assistanceId = purchase.assistanceId,
                        balanceBefore = purchase.balanceBefore,
                        balanceAfter = purchase.balanceAfter
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
        val cardId = purchase.smartcard
        return if (cardId != null) {
            api.postCardPurchase(cardId, convertToCardApi(purchase)).map { response ->
                Log.d(
                    TAG,
                    "Received code ${response.code()} when trying to sync purchase ${purchase.dbId} by ${purchase.smartcard}"
                )
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
                    purchasedProductDao.getProductsForPurchase(purchaseDb.dbId)
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
                                                dbId = purchaseDb.dbId,
                                                vendorId = purchaseDb.vendorId,
                                                beneficiaryId = cardPurchaseDb.beneficiaryId,
                                                assistanceId = cardPurchaseDb.assistanceId,
                                                currency = purchaseDb.currency,
                                                balanceBefore = cardPurchaseDb.balanceBefore,
                                                balanceAfter = cardPurchaseDb.balanceAfter
                                            )
                                            purchase.products.addAll(productsDb.map { convert(it) })
                                            purchase.vouchers.addAll(voucherPurchasesDb.map { it.voucher })
                                            Single.just(purchase)
                                        }
                                }
                        }
                }.toList()
        }
    }

    override fun addProductToCart(product: SelectedProduct): Completable {
        return if (product.product.category.type == CategoryType.CASHBACK) {
            selectedProductDao.getAll().flatMapCompletable { selectedProducts ->
                if (selectedProducts.none {
                        categoryRepo.getCategory(
                            productDao.getProductById(it.productId).categoryId
                        ).type == CategoryType.CASHBACK
                    }) {
                    selectedProductDao.insert(convertToDb(product))
                } else {
                    Log.e(TAG, "One cashback item already in cart")
                    Completable.complete()
                }
            }
        } else {
            selectedProductDao.insert(convertToDb(product))
        }
    }

    override fun getProductsFromCartObservable(): Observable<List<SelectedProduct>> {
        return selectedProductDao.getAllObservable().map { products ->
            products.map {
                convert(it)
            }
        }
    }

    override fun updateProductInCart(product: SelectedProduct): Completable {
        return selectedProductDao.update(product.dbId, product.price)
    }

    override fun removeProductFromCart(product: SelectedProduct): Completable {
        return selectedProductDao.delete(convertToDb(product)).doOnComplete {
            Log.d(TAG, "Removed product $product from cart")
        }
    }

    override fun deleteAllProductsInCart(): Completable {
        return selectedProductDao.deleteAll()
    }

    override fun deletePurchasedProducts(): Completable {
        return Completable.fromCallable {
            purchasedProductDao.deleteAll()
        }
    }

    override fun deletePurchase(purchase: Purchase): Completable {
        return Completable.fromCallable { purchaseDao.delete(convertToDb(purchase)) }
    }

    override fun deleteAllVoucherPurchases(): Completable {
        return getAllPurchases().flatMapCompletable { purchases ->
            Observable.fromIterable(purchases.filter { it.vouchers.isNotEmpty() })
                .flatMapCompletable {
                    Completable.fromCallable {
                        purchaseDao.delete(convertToDb(it))
                    }
                }
        }
    }

    override fun getPurchasesCount(): Observable<Long> {
        return purchaseDao.getCount()
    }

    private fun savePurchasedProducts(
        purchaseId: Long,
        products: List<PurchasedProduct>
    ): Completable {
        return Observable.fromIterable(products).flatMapCompletable { purchasedProduct ->
            Completable.fromCallable {
                purchasedProductDao.insert(convertToDb(purchasedProduct, purchaseId))
            }
        }
    }

    private fun convert(dbEntity: SelectedProductDbEntity): SelectedProduct {
        return SelectedProduct(
            dbId = dbEntity.dbId,
            product = convert(productDao.getProductById(dbEntity.productId)),
            price = dbEntity.value
        )
    }

    private fun convert(dbEntity: ProductDbEntity): Product {
        return Product().apply {
            this.id = dbEntity.id
            this.name = dbEntity.name
            this.image = dbEntity.image
            this.unit = dbEntity.unit
            this.category = categoryRepo.getCategory(dbEntity.categoryId)
            this.unitPrice = dbEntity.unitPrice
            this.currency = dbEntity.currency
        }
    }

    private fun convert(purchasedProductDbEntity: PurchasedProductDbEntity): PurchasedProduct {
        return PurchasedProduct(
            price = purchasedProductDbEntity.value,
            product = Product(
                id = purchasedProductDbEntity.productId
            )
        )
    }

    private fun convertToApi(
        purchased: PurchasedProduct,
        currency: String
    ): PurchasedProductApiEntity {
        return PurchasedProductApiEntity(
            id = purchased.product.id,
            value = purchased.price,
            currency = currency
        )
    }

    private fun convertToDb(purchasedProduct: SelectedProduct): SelectedProductDbEntity {
        return SelectedProductDbEntity(
            productId = purchasedProduct.product.id,
            value = purchasedProduct.price
        ).apply {
            purchasedProduct.dbId?.let { this.dbId = it }
        }
    }

    private fun convertToDb(
        purchasedProduct: PurchasedProduct,
        purchaseId: Long
    ): PurchasedProductDbEntity {
        return PurchasedProductDbEntity(
            productId = purchasedProduct.product.id,
            value = purchasedProduct.price,
            purchaseId = purchaseId
        )
    }

    private fun convertToDb(purchase: Purchase): PurchaseDbEntity {
        return PurchaseDbEntity(
            dbId = purchase.dbId,
            createdAt = purchase.createdAt,
            vendorId = purchase.vendorId,
            currency = purchase.currency
        )
    }

    private fun convertToCardApi(purchase: Purchase): CardPurchaseApiEntity {
        return CardPurchaseApiEntity(
            products = purchase.products.map { convertToApi(it, purchase.currency) },
            createdAt = purchase.createdAt,
            vendorId = purchase.vendorId,
            beneficiaryId = purchase.beneficiaryId,
            assistanceId = purchase.assistanceId,
            balanceBefore = purchase.balanceBefore,
            balanceAfter = purchase.balanceAfter
        )
    }

    private fun convertToVoucherApi(purchase: Purchase): VoucherPurchaseApiEntity {
        return VoucherPurchaseApiEntity(
            products = purchase.products.map { convertToApi(it, purchase.currency) },
            vouchers = purchase.vouchers,
            createdAt = purchase.createdAt,
            vendorId = purchase.vendorId
        )
    }

    companion object {
        private val TAG = PurchaseRepositoryImpl::class.java.simpleName
    }
}
