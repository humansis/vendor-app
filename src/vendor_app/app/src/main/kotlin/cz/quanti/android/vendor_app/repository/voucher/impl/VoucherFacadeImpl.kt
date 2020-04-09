package cz.quanti.android.vendor_app.repository.voucher.impl

import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.repository.product.ProductRepository
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.repository.voucher.VoucherRepository
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class VoucherFacadeImpl(
    private val voucherRepo: VoucherRepository,
    private val productRepo: ProductRepository
) : VoucherFacade {

    override fun saveVouchers(vouchers: List<Voucher>): Completable {
        return Observable.fromIterable(vouchers).flatMapCompletable { voucher ->
            voucherRepo.saveVoucher(voucher)
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

    private fun sendDataToServer(): Completable {
        return sendVouchers()
            .andThen(sendDeactivatedBooklets())
    }

    private fun getDataFromServer(): Completable {
        return reloadProductFromServer()
            .andThen(reloadDeactivatedBookletsFromServer())
            .andThen(reloadProtectedBookletsFromServer())
    }

    private fun sendVouchers(): Completable {
        return voucherRepo.getVouchers().flatMapCompletable { vouchers ->
            voucherRepo.sendVouchersToServer(vouchers).flatMapCompletable { responseCode ->
                if (isPositiveResponseHttpCode(responseCode)) {
                    voucherRepo.deleteAllVouchers()
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
