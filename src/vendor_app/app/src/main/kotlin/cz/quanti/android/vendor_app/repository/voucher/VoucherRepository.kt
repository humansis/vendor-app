package cz.quanti.android.vendor_app.repository.voucher

import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.VoucherPurchase
import io.reactivex.Completable
import io.reactivex.Single

interface VoucherRepository {
    fun getVoucherPurchases(): Single<List<VoucherPurchase>>

    fun saveVoucherPurchase(purchase: VoucherPurchase): Single<Long>

    fun deleteAllVoucherPurchases(): Completable

    fun deleteAllSelectedProducts(): Completable

    fun getAllDeactivatedBooklets(): Single<List<Booklet>>

    fun saveBooklet(booklet: Booklet): Completable

    fun getDeactivatedBookletsFromServer(): Single<Pair<Int, List<Booklet>>>

    fun getProtectedBookletsFromServer(): Single<Pair<Int, List<Booklet>>>

    fun getNewlyDeactivatedBooklets(): Single<List<Booklet>>

    fun deleteAllVouchers(): Completable

    fun deleteDeactivated(): Completable

    fun deleteProtected(): Completable

    fun deleteNewlyDeactivated(): Completable

    fun sendVoucherPurchasesToServer(purchase: List<VoucherPurchase>): Single<Int>

    fun sendDeactivatedBookletsToServer(booklets: List<Booklet>): Single<Int>

    fun getProtectedBooklets(): Single<List<Booklet>>

    fun saveSelectedProduct(selectedProduct: SelectedProduct, purchaseDbId: Long): Single<Long>

    fun saveVoucher(voucherId: Long, purchaseDbId: Long): Single<Long>
}
