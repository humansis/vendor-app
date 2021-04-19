package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface PurchaseRepository {

    fun savePurchase(purchase: Purchase): Completable

    fun sendCardPurchaseToServer(purchase: Purchase): Single<Int>

    fun sendVoucherPurchasesToServer(purchases: List<Purchase>): Single<Int>

    fun getAllPurchases(): Single<List<Purchase>>

    fun deleteAllPurchases(): Completable

    fun deleteCardPurchase(purchase: Purchase): Completable

    fun deleteVoucherPurchase(purchase: Purchase): Completable

    fun deleteAllVoucherPurchases(): Completable

    fun getPurchasesCount(): Single<Int>
}
