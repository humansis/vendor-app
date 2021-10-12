package cz.quanti.android.vendor_app.repository.deposit

import io.reactivex.Completable

interface DepositRepository {

    fun uploadDeposits(): Completable

    fun downloadDeposits(vendorId: Int): Completable
}
