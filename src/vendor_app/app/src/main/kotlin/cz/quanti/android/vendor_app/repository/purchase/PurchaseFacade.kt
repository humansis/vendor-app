package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Invoice
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.Transaction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface PurchaseFacade {

    fun savePurchase(purchase: Purchase): Completable

    fun syncWithServer(vendorId: Int): Completable

    fun unsyncedPurchases(): Single<List<Purchase>>

    fun getPurchasesCount(): Observable<Long>

    fun getInvoices(): Single<List<Invoice>>

    fun getTransactions(): Single<List<Transaction>>
}
