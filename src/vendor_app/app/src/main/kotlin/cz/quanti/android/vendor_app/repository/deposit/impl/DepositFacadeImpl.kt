package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.NullableObjectWrapper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject

class DepositFacadeImpl(
    private val depositRepo: DepositRepository
) : DepositFacade {

    override fun syncWithServer(
        syncSubjectReplaySubject: ReplaySubject<SynchronizationSubject>,
        vendorId: Int
    ): Completable {
        return sendDataToServer(syncSubjectReplaySubject)
            .andThen(loadDataFromServer(syncSubjectReplaySubject, vendorId))
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

    private fun sendDataToServer(syncSubjectReplaySubject: ReplaySubject<SynchronizationSubject>): Completable {
        return Completable.fromCallable {
            syncSubjectReplaySubject.onNext(SynchronizationSubject.RD_UPLOAD)
        }.andThen(depositRepo.uploadReliefPackages())
    }

    private fun loadDataFromServer(
        syncSubjectReplaySubject: ReplaySubject<SynchronizationSubject>,
        vendorId: Int
    ): Completable {
        return Completable.fromCallable {
            syncSubjectReplaySubject.onNext(SynchronizationSubject.RD_DOWNLOAD)
        }.andThen(depositRepo.downloadReliefPackages(vendorId))
    }

    companion object {
        private val TAG = DepositFacadeImpl::class.java.simpleName
    }
}
