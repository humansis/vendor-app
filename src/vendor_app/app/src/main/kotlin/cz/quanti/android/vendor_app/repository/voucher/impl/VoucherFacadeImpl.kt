package cz.quanti.android.vendor_app.repository.voucher.impl

import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.repository.voucher.VoucherRepository
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import io.reactivex.Completable
import io.reactivex.Observable

class VoucherFacadeImpl(private val voucherRepo: VoucherRepository) : VoucherFacade {

    override fun saveVouchers(vouchers: List<Voucher>): Completable {
        return Observable.fromIterable(vouchers).flatMapCompletable { voucher ->
            Completable.fromCallable { voucherRepo.saveVoucher(voucher) }
        }
    }
}
