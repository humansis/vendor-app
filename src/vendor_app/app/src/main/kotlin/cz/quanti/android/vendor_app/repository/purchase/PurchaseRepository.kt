package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.api.InvoiceApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.PurchaseApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.TransactionsApiEntity
import io.reactivex.Completable
import io.reactivex.Single

interface PurchaseRepository {

    fun savePurchase(purchase: Purchase): Completable

    fun sendCardPurchaseToServer(purchase: Purchase): Single<Int>

    fun sendVoucherPurchasesToServer(purchases: List<Purchase>): Single<Int>

    fun getAllPurchases(): Single<List<Purchase>>

    fun deleteAllPurchases(): Completable

    fun deletePurchase(purchase: Purchase):Completable

    fun deleteCardPurchase(purchase: Purchase): Completable

    fun deleteVoucherPurchase(purchase: Purchase): Completable

    fun deleteAllVoucherPurchases(): Completable

    fun getPurchasesCount(): Single<Int>

    fun getInvoices(vendorId: Int): Single<Pair<Int, List<InvoiceApiEntity>>>

    fun getTransactions(vendorId: Int): Single<Pair<Int, List<TransactionsApiEntity>>>

    fun getPurchasesById(purchaseIds: List<Int>): Single<Pair<Int, List<PurchaseApiEntity>>>
}
