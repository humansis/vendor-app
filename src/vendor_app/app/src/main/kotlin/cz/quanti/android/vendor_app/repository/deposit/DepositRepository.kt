package cz.quanti.android.vendor_app.repository.deposit

import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import io.reactivex.Completable
import io.reactivex.Single

interface DepositRepository {

    fun uploadReliefPackages(): Completable

    fun downloadReliefPackages(
        vendorId: Int
    ): Completable

    fun getReliefPackagesFromDb(tagId: String): Single<List<ReliefPackage>>

    fun deleteOldReliefPackages(): Completable

    fun updateReliefPackageInDb(reliefPackage: ReliefPackage): Completable
}
