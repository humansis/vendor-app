package cz.quanti.android.vendor_app.repository.purchase.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.product.dto.Product
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
    private val selectedProductDao: SelectedProductDao,
    private val invoiceDao: InvoiceDao,
    private val transactionDao: TransactionDao,
    private val transactionPurchaseDao: TransactionPurchaseDao,
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

    override fun retrieveInvoices(vendorId: Int): Single<Pair<Int, List<InvoiceApiEntity>>> {
        return api.getInvoices(vendorId).map { response ->
            var invoices = listOf<InvoiceApiEntity>()
            response.body()?.let { invoices = it.data }
            Pair(response.code(), invoices)
        }
    }

    override fun deleteInvoices(): Completable {
        return Completable.fromCallable { invoiceDao.deleteAll()}
    }

    override fun saveInvoice(invoice: InvoiceApiEntity): Single<Long> {
        return Single.fromCallable { invoiceDao.insert(convertToDb(invoice)) }
    }

    override fun getInvoices(): Single<List<Invoice>> {
        return invoiceDao.getAll().flatMap { invoicesDb ->
            Observable.fromIterable(invoicesDb)
                .flatMapSingle { invoiceDb ->
                    val invoice = Invoice(
                        invoiceId = invoiceDb.id,
                        date = invoiceDb.date,
                        quantity = invoiceDb.quantity,
                        value = invoiceDb.value,
                        currency = invoiceDb.currency
                    )
                    Single.just(invoice)
                }.toList()
        }
    }

    override fun getTransactions(): Single<List<Transaction>> {
        return transactionDao.getAll().flatMap { transactionsDb ->
            Observable.fromIterable(transactionsDb)
                .flatMapSingle { transactionDb ->
                    getTransactionPurchasesById(
                        getTransactionPurchaseIdsForTransaction(transactionDb.dbId)
                    ).flatMap { transactionPurchases ->
                        val transaction = Transaction(
                            //todo dodelat api request na endpoint aby se misto cisla projektu ukazoval jeho nazev
                            projectId = transactionDb.projectId,
                            purchases = transactionPurchases,
                            value = transactionDb.value,
                            currency = transactionDb.currency
                        )
                        Single.just(transaction)
                    }
                }.toList()
        }
    }

    private fun getTransactionPurchaseIdsForTransaction(transactionId: Long): List<Long> {
        val transactionPurchaseIds = mutableListOf<Long>()
        transactionPurchaseDao.getTransactionPurchaseForTransaction(transactionId).forEach {
            transactionPurchaseIds.add(it.dbId)
        }
        return transactionPurchaseIds
    }

    private fun getTransactionPurchasesById(purchaseIds: List<Long>): Single<List<TransactionPurchase>> {
        val transactionPurchases = mutableListOf<TransactionPurchase>()
        purchaseIds.forEach {
            val transactionPurchaseDb = transactionPurchaseDao.getTransactionPurchasesById(it)
                transactionPurchases.add(
                    TransactionPurchase(
                        purchaseId = transactionPurchaseDb.dbId,
                        transactionId = transactionPurchaseDb.transactionId,
                        beneficiaryId = transactionPurchaseDb.beneficiaryId,
                        createdAt = transactionPurchaseDb.createdAt,
                        value = transactionPurchaseDb.value,
                        currency = transactionPurchaseDb.currency
                    )
                )
        }
        return Single.just(transactionPurchases)
    }

    override fun retrieveTransactions(vendorId: Int): Single<Pair<Int, List<TransactionApiEntity>>> {
        return api.getTransactions(vendorId).map { response ->
            var transactions = listOf<TransactionApiEntity>()
            response.body()?.let { transactions = it.data }
            Pair(response.code(), transactions)
        }
    }

    override fun deleteTransactions(): Completable {
        return Completable.fromCallable { transactionDao.deleteAll() }
    }

    override fun saveTransaction(transaction: TransactionApiEntity, transactionId: Long): Single<Long> {
        return Single.fromCallable { transactionDao.insert(convertToDb(transaction, transactionId)) }
    }

    override fun retrieveTransactionsPurchasesById(purchaseIds: List<Int>): Single<Pair<Int, List<TransactionPurchaseApiEntity>>> {
        return api.getTransactionsPurchasesById(purchaseIds).map { response ->
            var transactionPurchases = listOf<TransactionPurchaseApiEntity>()
            response.body()?.let { transactionPurchases = it.data }
            Pair(response.code(), transactionPurchases)
        }
    }

    override fun deleteTransactionPurchases(): Completable {
        return Completable.fromCallable { transactionPurchaseDao.deleteAll() }
    }

    override fun saveTransactionPurchase(transactionPurchase: TransactionPurchaseApiEntity, transactionId: Long): Single<Long> {
        return Single.fromCallable { transactionPurchaseDao.insert(convertToDb(transactionPurchase, transactionId)) }
    }

    override fun deleteSelectedProducts(): Completable {
        return Completable.fromCallable {
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
        return Completable.fromCallable { purchaseDao.delete(convertToDb(purchase)) }
            .andThen( Completable.fromCallable {
                cardPurchaseDao.deleteCardForPurchase(purchase.dbId)
            })
    }

    override fun deleteVoucherPurchase(purchase: Purchase): Completable {
        return Completable.fromCallable { purchaseDao.delete(convertToDb(purchase)) }
            .andThen( Completable.fromCallable {
                voucherPurchaseDao.deleteVoucherForPurchase(purchase.dbId)
            })
    }

    override fun deleteAllVoucherPurchases(): Completable {
        return getAllPurchases().flatMapCompletable { purchases ->
            Observable.fromIterable(purchases.filter { it.vouchers.isNotEmpty()})
                .flatMapCompletable {
                    Completable.fromCallable {
                        purchaseDao.delete(convertToDb(it))
                    }
                }
        }.doOnComplete { (voucherPurchaseDao.deleteAll()) }
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

    private fun convertToDb(invoice: InvoiceApiEntity): InvoiceDbEntity {
        return InvoiceDbEntity(
            id = invoice.id,
            date = invoice.date,
            quantity = invoice.quantity,
            value = invoice.value,
            currency = invoice.currency
        )
    }

    private fun convertToDb(transaction: TransactionApiEntity, transactionId: Long): TransactionDbEntity {
        return TransactionDbEntity(
            dbId = transactionId,
            projectId = transaction.projectId,
            value = transaction.value,
            currency = transaction.currency
        )
    }

    private fun convertToDb(transactionPurchase: TransactionPurchaseApiEntity, transactionId: Long): TransactionPurchaseDbEntity {
        return TransactionPurchaseDbEntity(
            dbId = transactionPurchase.id,
            value = transactionPurchase.value,
            currency = transactionPurchase.currency,
            beneficiaryId = transactionPurchase.beneficiaryId,
            createdAt = transactionPurchase.dateOfPurchase,
            transactionId = transactionId
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

