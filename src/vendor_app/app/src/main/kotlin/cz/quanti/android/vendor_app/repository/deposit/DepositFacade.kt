package cz.quanti.android.vendor_app.repository.deposit

import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.NullableObjectWrapper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DepositFacade {
    fun syncWithServer(
        vendorId: Int
    ): Observable<SynchronizationSubject>

    fun deleteReliefPackageFromDB(id: Int): Completable

    fun updateReliefPackageInDB(reliefPackage: ReliefPackage): Completable

    fun getRelevantReliefPackage(tagId: String): Single<NullableObjectWrapper<ReliefPackage>>
}
