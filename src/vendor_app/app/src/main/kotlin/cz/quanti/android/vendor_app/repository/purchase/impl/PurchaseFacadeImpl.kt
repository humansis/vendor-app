package cz.quanti.android.vendor_app.repository.purchase.impl

import cz.quanti.android.vendor_app.repository.card.CardRepository
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseRepository
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.sync.SynchronizationManagerImpl
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.BlockedCardError
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import quanti.com.kotlinlog.Log

class PurchaseFacadeImpl(
    private val purchaseRepo: PurchaseRepository,
    private val cardRepo: CardRepository
) : PurchaseFacade {

    override fun savePurchase(purchase: Purchase): Completable {
        return cardRepo.isBlockedCard(purchase.smartcard).flatMapCompletable { itsBlocked ->
            if (itsBlocked) {
                throw BlockedCardError("This card is tagged as blocked on the server")
            } else {
                purchaseRepo.savePurchase(purchase)
            }
        }
    }

    override fun syncWithServer(): Observable<SynchronizationSubject> {
        return Observable.just(SynchronizationSubject.PURCHASES_UPLOAD)
            .concatWith(preparePurchases())
            .concatWith(sendPurchasesToServer())
            .concatWith(deletePurchasedProducts())
    }

    override fun getPurchasesCount(): Observable<Long> {
        return purchaseRepo.getPurchasesCount()
    }

    override fun addProductToCart(product: SelectedProduct): Completable {
        return purchaseRepo.addProductToCart(product)
    }

    override fun getProductsFromCartSingle(): Single<List<SelectedProduct>> {
        return purchaseRepo.getProductsFromCartObservable().firstOrError()
    }

    override fun getProductsFromCartObservable(): Observable<List<SelectedProduct>> {
        return purchaseRepo.getProductsFromCartObservable()
    }

    override fun updateProductInCart(product: SelectedProduct): Completable {
        return purchaseRepo.updateProductInCart(product)
    }

    override fun removeProductFromCart(product: SelectedProduct): Completable {
        return purchaseRepo.removeProductFromCart(product)
    }

    override fun deleteAllProductsInCart(): Completable {
        return purchaseRepo.deleteAllProductsInCart()
    }

    private fun preparePurchases(): Completable {
        return purchaseRepo.getAllPurchases().flatMapCompletable { purchases ->
            Observable.fromIterable(purchases.filter { it.products.isEmpty() || it.smartcard.isNullOrBlank() })
                .flatMapCompletable {
                    Log.d(
                        TAG,
                        "Purchase ${it.dbId} created at ${it.createdAt} has no products or payment methods"
                    )
                    purchaseRepo.deletePurchase(it)
                }
        }.onErrorResumeNext {
            Completable.error(
                SynchronizationManagerImpl.ExceptionWithReason(
                    it,
                    "Failed preparing purchases"
                )
            )
        }
    }

    private fun sendPurchasesToServer(): Completable {
        val invalidPurchases = mutableListOf<Purchase>()
        return purchaseRepo.getAllPurchases().flatMapCompletable { purchases ->
            if (purchases.isEmpty()) {
                Log.d(TAG, "No purchases to upload")
                Completable.complete()
            } else {
                Observable.fromIterable(purchases.filter { it.smartcard != null })
                    .flatMapCompletable { purchase ->
                        purchaseRepo.sendCardPurchaseToServer(purchase)
                            .flatMapCompletable { responseCode ->
                                if (isPositiveResponseHttpCode(responseCode)) {
                                    purchaseRepo.deletePurchase(purchase).doOnComplete {
                                        Log.d(
                                            TAG,
                                            "Purchase ${purchase.dbId} by ${purchase.smartcard} successfully removed from db"
                                        )
                                    }
                                } else {
                                    invalidPurchases.add(purchase)
                                    Completable.complete()
                                }
                            }
                    }
                    // throw exception after all purchases has been iterated
                    .doOnComplete {
                        Log.d(
                            TAG,
                            "Smartcard purchases sync finished successfully"
                        )
                        if (invalidPurchases.isNotEmpty()) {
                            throw VendorAppException("Could not send purchases to the server").apply {
                                apiError = true
                            }
                        }
                    }
            }
        }.onErrorResumeNext {
            Completable.error(
                SynchronizationManagerImpl.ExceptionWithReason(
                    it,
                    "Failed uploading purchases"
                )
            )
        }
    }

    private fun deletePurchasedProducts(): Completable {
        return purchaseRepo.deletePurchasedProducts()
            .onErrorResumeNext {
                Completable.error(
                    SynchronizationManagerImpl.ExceptionWithReason(
                        it,
                        "Failed deleting purchased products"
                    )
                )
            }
    }

    companion object {
        private val TAG = PurchaseFacadeImpl::class.java.simpleName
    }
}
