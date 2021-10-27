package cz.quanti.android.vendor_app.repository.deposit

import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import io.reactivex.Completable
import io.reactivex.Single

interface DepositRepository {

    fun downloadReliefPackages(vendorId: Int): Single<Pair<Int, List<ReliefPackage>>>

    fun saveReliefPackagesToDB(reliefPackages: List<ReliefPackage>): Completable

    fun getReliefPackageFromDB(tagId: String): Single<List<ReliefPackage?>>

    fun deleteReliefPackagesFromDB(): Completable

    fun deleteReliefPackageFromDB(id: Int): Completable

    fun updateReliefPackageInDB(reliefPackage: ReliefPackage): Completable

    fun getDistributedReliefPackages(): Single<List<ReliefPackage>>

    fun postReliefPackages(reliefPackages: List<ReliefPackage>): Single<Int>
}
