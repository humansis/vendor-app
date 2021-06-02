package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Invoice
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.Transaction
import cz.quanti.android.vendor_app.repository.purchase.dto.TransactionPurchase
import cz.quanti.android.vendor_app.repository.purchase.dto.api.InvoiceApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.TransactionPurchaseApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.TransactionApiEntity
import io.reactivex.Completable
import io.reactivex.Single

interface PurchaseRepository {

    fun savePurchase(purchase: Purchase): Completable

    fun sendCardPurchaseToServer(purchase: Purchase): Single<Int>

    fun sendVoucherPurchasesToServer(purchases: List<Purchase>): Single<Int>

    fun getAllPurchases(): Single<List<Purchase>>

    fun deleteSelectedProducts(): Completable

    fun deletePurchase(purchase: Purchase): Completable

    fun deleteCardPurchase(purchase: Purchase): Completable

    fun deleteVoucherPurchase(purchase: Purchase): Completable

    fun deleteAllVoucherPurchases(): Completable

    fun getPurchasesCount(): Single<Int>

    fun retrieveInvoices(vendorId: Int): Single<Pair<Int, List<InvoiceApiEntity>>>

    fun deleteInvoices(): Completable

    fun saveInvoice(invoice: InvoiceApiEntity): Single<Long>

    fun getInvoices(): Single<List<Invoice>>

    fun retrieveTransactions(vendorId: Int): Single<Pair<Int, List<TransactionApiEntity>>>

    fun deleteTransactions(): Completable

    fun saveTransaction(transaction: TransactionApiEntity, transactionId: Long): Single<Long>

    fun getTransactions(): Single<List<Transaction>>

    fun retrieveTransactionsPurchasesById(purchaseIds: List<Int>): Single<Pair<Int, List<TransactionPurchaseApiEntity>>>

    fun deleteTransactionPurchases(): Completable

    fun saveTransactionPurchase(
        transactionPurchase: TransactionPurchaseApiEntity,
        transactionId: Long
    ): Single<Long>
}
