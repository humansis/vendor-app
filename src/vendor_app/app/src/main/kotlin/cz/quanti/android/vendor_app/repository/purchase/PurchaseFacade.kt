package cz.quanti.android.vendor_app.repository.purchase

import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import io.reactivex.Completable

interface PurchaseFacade {

    fun savePurchase(purchase: Purchase): Completable

    fun syncWithServer(): Completable
}
