package cz.quanti.android.vendor_app.repository.deposit

import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import io.reactivex.Completable
import io.reactivex.Single

interface DepositFacade {
    fun syncWithServer(vendorId: Int): Completable

    fun getDepositByTag(tagId: String): Single<ReliefPackage?>

    fun deleteReliefPackageFromDB(id: Int): Completable

    fun updateReliefPackageInDB(reliefPackage: ReliefPackage): Completable
}
