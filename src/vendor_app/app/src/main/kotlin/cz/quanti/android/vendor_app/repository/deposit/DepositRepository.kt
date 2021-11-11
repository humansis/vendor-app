package cz.quanti.android.vendor_app.repository.deposit

import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

interface DepositRepository {

    fun uploadReliefPackages(): Completable

    fun downloadReliefPackages(vendorId: Int): Completable

    fun getReliefPackagesFromDB(tagId: String): Single<List<ReliefPackage>>

    fun deleteReliefPackageFromDB(id: Int): Completable

    fun deleteOldReliefPackages(): Completable

    fun updateReliefPackageInDB(reliefPackage: ReliefPackage): Completable
}
