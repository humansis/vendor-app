package cz.quanti.android.vendor_app.repository.purchase.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.category.dao.CategoryDao
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.product.dao.ProductDao
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import cz.quanti.android.vendor_app.repository.purchase.PurchaseRepository
import cz.quanti.android.vendor_app.repository.purchase.dao.*
import cz.quanti.android.vendor_app.repository.purchase.dto.*
import cz.quanti.android.vendor_app.repository.purchase.dto.api.*
import cz.quanti.android.vendor_app.repository.purchase.dto.db.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import quanti.com.kotlinlog.Log

class PurchaseRepositoryImpl(
    private val purchaseDao: PurchaseDao,
    private val cardPurchaseDao: CardPurchaseDao,
    private val voucherPurchaseDao: VoucherPurchaseDao,
    private val categoryDao: CategoryDao,
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
                        beneficiaryId = purchase.beneficiaryId
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
                                                beneficiaryId = cardPurchaseDb.beneficiaryId
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

    override fun addProductToCart(product: SelectedProduct) {
        if (product.category.type == CategoryType.CASHBACK) {
            if (selectedProductDao.getAll().none {
                categoryDao.getCategoryById(it.categoryId).type == CategoryType.CASHBACK.name
            }) {
                selectedProductDao.insert(convertToDb(product))
            } else {
                Log.e(TAG, "One cashback item already in cart")
            }
        } else {
            selectedProductDao.insert(convertToDb(product))
        }
    }

    override fun getProductsFromCart(): Observable<List<SelectedProduct>> {
        return selectedProductDao.getAllObservable().map { products ->
            products.map {
                convert(it)
            }
        }
    }

    override fun updateProductInCart(product: SelectedProduct) {
        selectedProductDao.update(product.dbId, product.price)
    }

    override fun removeProductFromCartAt(product: SelectedProduct) {
        selectedProductDao.delete(convertToDb(product))
    }

    override fun deleteAllProductsInCart() {
        selectedProductDao.deleteAll()
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
            Observable.fromIterable(purchases.filter { it.vouchers.isNotEmpty()})
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
        val categoryDb = categoryDao.getCategoryById(dbEntity.categoryId)
        return SelectedProduct(
            dbId = dbEntity.dbId,
            product = convert(productDao.getProductById(dbEntity.productId)),
            price = dbEntity.value,
            category = Category(
                id = categoryDb.id,
                name = categoryDb.name,
                type = CategoryType.valueOf(categoryDb.type),
                image = categoryDb.image
            ),
            currency = dbEntity.currency
        )
    }

    private fun convert(dbEntity: ProductDbEntity): Product {
        return Product().apply {
            this.id = dbEntity.id
            this.name = dbEntity.name
            this.image = dbEntity.image
            this.unit = dbEntity.unit
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

    private fun convertToApi(purchased: PurchasedProduct, currency: String): PurchasedProductApiEntity {
        return PurchasedProductApiEntity(
            id = purchased.product.id,
            value = purchased.price,
            currency = currency
        )
    }

    private fun convertToDb(purchasedProduct: SelectedProduct): SelectedProductDbEntity {
        return SelectedProductDbEntity(
                productId = purchasedProduct.product.id,
                value = purchasedProduct.price,
            ).apply {
                purchasedProduct.dbId?.let { this.dbId = it }
                this.categoryId = purchasedProduct.category.id
                this.currency = purchasedProduct.currency
            }
    }

    private fun convertToDb(purchasedProduct: PurchasedProduct, purchaseId: Long): PurchasedProductDbEntity {
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

