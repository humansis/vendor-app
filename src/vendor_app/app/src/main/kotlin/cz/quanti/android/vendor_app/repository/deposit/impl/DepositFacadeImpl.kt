package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import cz.quanti.android.vendor_app.sync.SynchronizationManagerImpl
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

    override fun updateReliefPackageInDB(reliefPackage: ReliefPackage): Completable {
        return depositRepo.updateReliefPackageInDb(reliefPackage)
    }

    override fun getRelevantReliefPackage(tagId: String): Single<NullableObjectWrapper<ReliefPackage>> {
        return depositRepo.deleteOldReliefPackages().andThen(
            depositRepo.getReliefPackagesFromDb(tagId).map { reliefPackages ->
                NullableObjectWrapper(
                    reliefPackages
                        .filter { it.createdAt == null }
                        .sortedWith(nullsLast(compareBy { it.expirationDate }))
                        .firstOrNull()
                )
            }
        )
    }

    private fun sendDataToServer(): Completable {
        return depositRepo.uploadReliefPackages()
            .onErrorResumeNext {
                Completable.error(
                    SynchronizationManagerImpl.ExceptionWithReason(
                        it,
                        "Failed uploading RD"
                    )
                )
            }
    }

    private fun loadDataFromServer(
        vendorId: Int
    ): Completable {
        return depositRepo.downloadReliefPackages(vendorId)
            .onErrorResumeNext {
                Completable.error(
                    SynchronizationManagerImpl.ExceptionWithReason(
                        it,
                        "Failed downloading RD"
                    )
                )
            }
    }
}
