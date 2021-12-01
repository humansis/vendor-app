package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.NullableObjectWrapper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class DepositFacadeImpl(
    private val depositRepo: DepositRepository
) : DepositFacade {

    override fun syncWithServer(
        vendorId: Int
    ): Observable<SynchronizationSubject> {
        return Observable.just(SynchronizationSubject.RD_UPLOAD)
            .concatWith(sendDataToServer())
            .concatWith(Observable.just(SynchronizationSubject.RD_DOWNLOAD))
            .concatWith(loadDataFromServer(vendorId))
    }

    override fun deleteReliefPackageFromDB(id: Int): Completable {
        return depositRepo.deleteReliefPackageFromDB(id)
    }

    override fun updateReliefPackageInDB(reliefPackage: ReliefPackage): Completable {
        return depositRepo.updateReliefPackageInDB(reliefPackage)
    }

    override fun getRelevantReliefPackage(tagId: String): Single<NullableObjectWrapper<ReliefPackage>> {
        return depositRepo.deleteOldReliefPackages().andThen(
            depositRepo.getReliefPackagesFromDB(tagId).map { reliefPackages ->
                NullableObjectWrapper(
                    reliefPackages.sortedWith(nullsLast(compareBy { it.expirationDate }))
                        .firstOrNull()
                )
            }
        )
    }

    private fun sendDataToServer(): Completable {
        return depositRepo.uploadReliefPackages()
    }

    private fun loadDataFromServer(
        vendorId: Int
    ): Completable {
        return depositRepo.downloadReliefPackages(vendorId)
    }
}
