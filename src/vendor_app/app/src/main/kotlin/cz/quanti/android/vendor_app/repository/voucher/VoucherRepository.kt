package cz.quanti.android.vendor_app.repository.voucher

import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import io.reactivex.Completable

interface VoucherRepository {
    fun saveVoucher(voucher: Voucher): Completable
}
