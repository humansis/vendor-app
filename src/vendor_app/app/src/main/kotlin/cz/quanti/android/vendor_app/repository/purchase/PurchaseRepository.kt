package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface PurchaseRepository {

    fun savePurchase(purchase: Purchase): Completable

    fun sendCardPurchaseToServer(purchases: Purchase): Maybe<Int>

    fun sendVoucherPurchasesToServer(purchases: List<Purchase>): Maybe<Int>

    fun getAllPurchases(): Single<List<Purchase>>

    fun deleteAllPurchases(): Completable
}
