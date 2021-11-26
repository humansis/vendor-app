package cz.quanti.android.vendor_app.repository.deposit

import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.NullableObjectWrapper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject

interface DepositFacade {
    fun syncWithServer(
        syncSubjectReplaySubject: ReplaySubject<SynchronizationSubject>,
        vendorId: Int
    ): Completable

    fun deleteReliefPackageFromDB(id: Int): Completable

    fun updateReliefPackageInDB(reliefPackage: ReliefPackage): Completable

    fun getRelevantReliefPackage(tagId: String): Single<NullableObjectWrapper<ReliefPackage>>
}
