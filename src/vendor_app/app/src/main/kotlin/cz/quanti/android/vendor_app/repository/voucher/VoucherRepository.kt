package cz.quanti.android.vendor_app.repository.voucher

import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import io.reactivex.Completable
import io.reactivex.Single

interface VoucherRepository {
    fun getVouchers(): Single<List<Voucher>>

    fun saveVoucher(voucher: Voucher): Completable

    fun getAllDeactivatedBooklets(): Single<List<Booklet>>

    fun saveBooklet(booklet: Booklet): Completable

    fun getDeactivatedBookletsFromServer(): Single<Pair<Int, List<Booklet>>>

    fun getProtectedBookletsFromServer(): Single<Pair<Int, List<Booklet>>>

    fun getNewlyDeactivatedBooklets(): Single<List<Booklet>>

    fun deleteDeactivated(): Completable

    fun deleteProtected(): Completable

    fun deleteNewlyDeactivated(): Completable

    fun sendVouchersToServer(vouchers: List<Voucher>): Single<Int>

    fun sendDeactivatedBookletsToServer(booklets: List<Booklet>): Single<Int>
}
