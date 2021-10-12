package cz.quanti.android.vendor_app.repository.deposit

import io.reactivex.Completable

interface DepositFacade {
    fun syncWithServer(vendorId: Int): Completable
}
