package cz.quanti.android.vendor_app.repository.voucher.impl

import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.repository.product.ProductRepository
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.repository.voucher.VoucherRepository
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.VoucherPurchase
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class VoucherFacadeImpl(
    private val voucherRepo: VoucherRepository,
    private val productRepo: ProductRepository
) : VoucherFacade {

    override fun saveVoucherPurchase(purchase: VoucherPurchase): Completable {
        return voucherRepo.saveVoucherPurchase(purchase).flatMapCompletable { purchaseDbId ->
            saveSelectedProducts(purchase.products, purchaseDbId)
                .andThen(saveVouchers(purchase.vouchers, purchaseDbId))
        }
    }

    override fun getAllDeactivatedBooklets(): Single<List<Booklet>> {
        return voucherRepo.getAllDeactivatedBooklets()
    }

    override fun deactivate(booklet: String): Completable {
        return voucherRepo.saveBooklet(Booklet().apply {
            this.code = booklet
            this.state = Booklet.STATE_NEWLY_DEACTIVATED
        })
    }

    override fun getProtectedBooklets(): Single<List<Booklet>> {
        return voucherRepo.getProtectedBooklets()
    }

    override fun clearVouchers(): Completable {
        return voucherRepo.deleteAllVouchers()
    }


    override fun syncWithServer(): Completable {
        return sendDataToServer()
            .andThen(getDataFromServer())
    }

    private fun saveSelectedProducts(
        products: List<SelectedProduct>,
        purchaseId: Long
    ): Completable {
        return Observable.fromIterable(products)
            .flatMapCompletable { product ->
                voucherRepo.saveSelectedProduct(product, purchaseId).ignoreElement()
            }
    }

    private fun saveVouchers(voucherIds: List<Long>, purchaseId: Long): Completable {
        return Observable.fromIterable(voucherIds)
            .flatMapCompletable { voucherId ->
                voucherRepo.saveVoucher(voucherId, purchaseId).ignoreElement()
            }
    }

    private fun sendDataToServer(): Completable {
        return sendVoucherPurchases()
            .andThen(sendDeactivatedBooklets())
            .andThen(clearDb())
    }

    private fun clearDb(): Completable {
        return voucherRepo.deleteAllSelectedProducts()
    }

    private fun getDataFromServer(): Completable {
        return reloadProductFromServer()
            .andThen(reloadDeactivatedBookletsFromServer())
            .andThen(reloadProtectedBookletsFromServer())
    }

    private fun sendVoucherPurchases(): Completable {
        return voucherRepo.getVoucherPurchases().flatMapCompletable { purchases ->
            voucherRepo.sendVoucherPurchasesToServer(purchases).flatMapCompletable { responseCode ->
                if (isPositiveResponseHttpCode(responseCode)) {
                    voucherRepo.deleteAllVouchers()
                        .andThen(voucherRepo.deleteAllVoucherPurchases())
                } else {
                    throw VendorAppException("Could not send vouchers to server").apply {
                        apiError = true
                        apiResponseCode = responseCode
                    }
                }
            }
        }
    }

    private fun sendDeactivatedBooklets(): Completable {
        return voucherRepo.getNewlyDeactivatedBooklets().flatMapCompletable { booklets ->
            voucherRepo.sendDeactivatedBookletsToServer(booklets)
                .flatMapCompletable { responseCode ->
                    if (isPositiveResponseHttpCode(responseCode)) {
                        Completable.complete()
                    } else {
                        throw VendorAppException("Could not send booklets to server").apply {
                            apiError = true
                            apiResponseCode = responseCode
                        }
                    }
                }
        }
    }

    private fun reloadProductFromServer(): Completable {
        return productRepo.getProductsFromServer().flatMapCompletable { response ->
            val responseCode = response.first
            val products = response.second

            if (isPositiveResponseHttpCode(responseCode)) {
                actualizeDatabase(products)
            } else {
                Completable.error(
                    VendorAppException(
                        "Could not get products from server."
                    ).apply {
                        apiError = true
                        apiResponseCode = responseCode
                    })
            }
        }
    }

    private fun actualizeDatabase(products: List<Product>): Completable {
        return if (products.isEmpty()) {
            throw VendorAppException("Products returned from server were empty.")
        } else {
            productRepo.deleteProducts().andThen(
                Observable.fromIterable(products).flatMapCompletable { product ->
                    productRepo.saveProduct(product)
                        .andThen(Completable.fromCallable { Picasso.get().load(product.image) })
                })
        }
    }

    private fun reloadDeactivatedBookletsFromServer(): Completable {
        return voucherRepo.getDeactivatedBookletsFromServer().flatMapCompletable { response ->
            val responseCode = response.first
            if (isPositiveResponseHttpCode(responseCode)) {
                val booklets = response.second
                voucherRepo.deleteDeactivated()
                    .andThen(Observable.fromIterable(booklets).flatMapCompletable { booklet ->
                        booklet.state = Booklet.STATE_DEACTIVATED
                        voucherRepo.saveBooklet(booklet)
                    })
            } else {
                throw VendorAppException("Could not load deactivated booklets").apply {
                    this.apiResponseCode = responseCode
                    this.apiError = true
                }
            }
        }
    }

    private fun reloadProtectedBookletsFromServer(): Completable {
        return voucherRepo.getProtectedBookletsFromServer().flatMapCompletable { response ->
            val responseCode = response.first

            if (isPositiveResponseHttpCode(responseCode)) {
                val booklets = response.second
                voucherRepo.deleteProtected()
                    .andThen(Observable.fromIterable(booklets).flatMapCompletable { booklet ->
                        booklet.state = Booklet.STATE_PROTECTED
                        voucherRepo.saveBooklet(booklet)
                    })
            } else {
                throw VendorAppException("Could not load protected booklets").apply {
                    this.apiResponseCode = responseCode
                    this.apiError = true
                }
            }
        }
    }
}
