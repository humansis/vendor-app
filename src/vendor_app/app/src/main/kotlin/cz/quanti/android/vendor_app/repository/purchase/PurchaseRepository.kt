package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.purchase.dto.Transaction
import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.repository.purchase.dto.api.InvoiceApiEntity
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionApiEntity
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionPurchaseApiEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface PurchaseRepository {

    fun savePurchase(purchase: Purchase): Completable

    fun sendCardPurchaseToServer(purchase: Purchase): Single<Int>

    fun sendVoucherPurchasesToServer(purchases: List<Purchase>): Single<Int>

    fun getAllPurchases(): Single<List<Purchase>>

    fun deletePurchasedProducts(): Completable

    fun deletePurchase(purchase: Purchase): Completable

    fun deleteAllVoucherPurchases(): Completable

    fun getPurchasesCount(): Observable<Long>

    fun retrieveInvoices(vendorId: Int): Single<Pair<Int, List<InvoiceApiEntity>>>

    fun deleteInvoices(): Completable

    fun saveInvoice(invoice: InvoiceApiEntity): Single<Long>

    fun getInvoices(): Single<List<Invoice>>

    fun retrieveTransactions(vendorId: Int): Single<Pair<Int, List<TransactionApiEntity>>>

    fun deleteTransactions(): Completable

    fun saveTransaction(transaction: TransactionApiEntity, transactionId: Long): Single<Long>

    fun getTransactions(): Single<List<Transaction>>

    fun retrieveTransactionsPurchases(vendorId: Int, projectId: Long, currency: String): Single<Pair<Int, List<TransactionPurchaseApiEntity>>>

    fun deleteTransactionPurchases(): Completable

    fun saveTransactionPurchase(
        transactionPurchase: TransactionPurchaseApiEntity,
        transactionId: Long
    ): Single<Long>

    fun addProductToCart(product: SelectedProduct)

    fun getProductsFromCart(): Observable<List<SelectedProduct>>

    fun updateProductInCart(product: SelectedProduct)

    fun removeProductFromCartAt(product: SelectedProduct)

    fun deleteAllProductsInCart()
}
