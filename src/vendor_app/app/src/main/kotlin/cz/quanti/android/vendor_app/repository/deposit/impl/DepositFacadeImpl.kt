package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import io.reactivex.Completable

class DepositFacadeImpl(
    private val depositRepo: DepositRepository
): DepositFacade {

    override fun syncWithServer(vendorId: Int): Completable {
        return sendDataToServer()
            .andThen(loadDataFromServer(vendorId))
    }

    private fun sendDataToServer(): Completable {
        return depositRepo.uploadDeposits()
    }

    private fun loadDataFromServer(vendorId: Int): Completable {
        return depositRepo.downloadDeposits(vendorId)
    }
}
