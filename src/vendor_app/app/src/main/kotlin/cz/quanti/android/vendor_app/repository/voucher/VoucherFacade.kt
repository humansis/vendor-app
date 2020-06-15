package cz.quanti.android.vendor_app.repository.voucher

import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.VoucherPurchase
import io.reactivex.Completable
import io.reactivex.Single

interface VoucherFacade {

    fun saveVoucherPurchase(purchase: VoucherPurchase): Completable

    fun getAllDeactivatedBooklets(): Single<List<Booklet>>

    fun deactivate(booklet: String): Completable

    fun getProtectedBooklets(): Single<List<Booklet>>

    fun clearVouchers(): Completable

    fun syncWithServer(): Completable
}
