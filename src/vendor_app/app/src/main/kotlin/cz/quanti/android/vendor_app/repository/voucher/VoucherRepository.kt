package cz.quanti.android.vendor_app.repository.voucher

import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import io.reactivex.Completable
import io.reactivex.Single

interface VoucherRepository {
    fun saveVoucher(voucher: Voucher): Completable

    fun getDeactivatedBooklets(): Single<List<Booklet>>
}
