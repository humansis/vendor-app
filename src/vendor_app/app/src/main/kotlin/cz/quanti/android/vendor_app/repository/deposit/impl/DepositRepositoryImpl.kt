package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dao.ReliefPackageDao
import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import cz.quanti.android.vendor_app.repository.deposit.dto.api.ReliefPackageApiEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.api.SmartcardDepositApiEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.db.ReliefPackageDbEntity
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.convertStringToDate
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date
import quanti.com.kotlinlog.Log

class DepositRepositoryImpl(
    private val reliefPackageDao: ReliefPackageDao,
    private val api: VendorAPI
) : DepositRepository {

    override fun uploadReliefPackages(): Completable {
        return getDistributedReliefPackages().flatMapCompletable { reliefPackages ->
            if (reliefPackages.isNotEmpty()) {
                postReliefPackages(reliefPackages).flatMapCompletable { responseCode ->
                    if (isPositiveResponseHttpCode(responseCode)) {
                        deleteReliefPackagesFromDB()
                    } else {
                        throw VendorAppException("Could not upload RD").apply {
                            this.apiResponseCode = responseCode
                            this.apiError = true
                        }
                    }
                }
            } else {
                Completable.complete()
            }
        }
    }

    override fun downloadReliefPackages(vendorId: Int): Completable {
        return api.getReliefPackages(vendorId, PACKAGE_STATE_TO_DISTRIBUTE)
            .flatMapCompletable { response ->
                when {
                    isPositiveResponseHttpCode(response.code()) -> {
                        if (response.body()?.data.isNullOrEmpty()) {
                            Log.d("RD returned from server were empty.")
                            Completable.complete()
                        } else {
                            response.body()?.data?.let { data ->
                                actualizeDatabase(data.map {
                                    convert(it)
                                })
                            }
                        }
                    }
                    response.code() == 403 -> {
                        Log.d(TAG, "RD sync denied")
                        Completable.complete()
                    }
                    else -> {
                        throw VendorAppException("Could not download RD").apply {
                            this.apiResponseCode = response.code()
                            this.apiError = true
                        }
                    }
                }
            }
    }

    private fun actualizeDatabase(reliefPackages: List<ReliefPackage>): Completable {
        return deleteReliefPackagesFromDB().andThen(
            saveReliefPackagesToDB(reliefPackages)
        )
    }

    private fun saveReliefPackagesToDB(reliefPackages: List<ReliefPackage>): Completable {
        return Observable.fromIterable(reliefPackages).flatMapCompletable {
            saveReliefPackageToDB(it)
        }
    }

    private fun saveReliefPackageToDB(reliefPackage: ReliefPackage): Completable {
        return reliefPackageDao.insert(convert(reliefPackage))
    }

    override fun getReliefPackagesFromDB(tagId: String): Single<List<ReliefPackage>> {
        return reliefPackageDao.getReliefPackagesByTagId(tagId).map { list ->
            list.map {
                convert(it)
            }
        }
    }

    private fun deleteReliefPackagesFromDB(): Completable {
        return reliefPackageDao.deleteAll()
    }

    override fun deleteOldReliefPackages(): Completable {
        return reliefPackageDao.deleteOlderThan(Date())
    }

    override fun deleteReliefPackageFromDB(id: Int): Completable {
        return reliefPackageDao.getReliefPackageById(id).flatMapCompletable {
            reliefPackageDao.delete(it)
        }
    }

    override fun updateReliefPackageInDB(reliefPackage: ReliefPackage): Completable {
        return reliefPackageDao.update(
            reliefPackage.id,
            reliefPackage.createdAt,
            reliefPackage.balanceBefore,
            reliefPackage.balanceAfter
        )
    }

    private fun getDistributedReliefPackages(): Single<List<ReliefPackage>> {
        return reliefPackageDao.getDistributedReliefPackages().map { list ->
            list.map {
                convert(it)
            }
        }
    }

    private fun postReliefPackages(reliefPackages: List<ReliefPackage>): Single<Int> {
        return api.postReliefPackages(reliefPackages.map {
            convertForPost(it)
        }).map {
            it.code()
        }
    }

    private fun convert(reliefPackageApiEntity: ReliefPackageApiEntity): ReliefPackage {
        return ReliefPackage(
            id = reliefPackageApiEntity.id,
            assistanceId = reliefPackageApiEntity.assistanceId,
            beneficiaryId = reliefPackageApiEntity.beneficiaryId,
            amount = reliefPackageApiEntity.amountToDistribute,
            currency = reliefPackageApiEntity.unit,
            tagId = reliefPackageApiEntity.smartCardSerialNumber,
            foodLimit = reliefPackageApiEntity.foodLimit,
            nonfoodLimit = reliefPackageApiEntity.nonfoodLimit,
            cashbackLimit = reliefPackageApiEntity.cashbackLimit,
            expirationDate = convertStringToDate(reliefPackageApiEntity.expirationDate)
        )
    }

    private fun convert(reliefPackage: ReliefPackage): ReliefPackageDbEntity {
        return ReliefPackageDbEntity(
            id = reliefPackage.id,
            assistanceId = reliefPackage.assistanceId,
            beneficiaryId = reliefPackage.beneficiaryId,
            amount = reliefPackage.amount,
            currency = reliefPackage.currency,
            tagId = reliefPackage.tagId,
            foodLimit = reliefPackage.foodLimit,
            nonfoodLimit = reliefPackage.nonfoodLimit,
            cashbackLimit = reliefPackage.cashbackLimit,
            expirationDate = reliefPackage.expirationDate
        )
    }

    private fun convert(reliefPackageDbEntity: ReliefPackageDbEntity): ReliefPackage {
        return ReliefPackage(
            id = reliefPackageDbEntity.id,
            assistanceId = reliefPackageDbEntity.assistanceId,
            beneficiaryId = reliefPackageDbEntity.beneficiaryId,
            amount = reliefPackageDbEntity.amount,
            currency = reliefPackageDbEntity.currency,
            tagId = reliefPackageDbEntity.tagId,
            foodLimit = reliefPackageDbEntity.foodLimit,
            nonfoodLimit = reliefPackageDbEntity.nonfoodLimit,
            cashbackLimit = reliefPackageDbEntity.cashbackLimit,
            expirationDate = reliefPackageDbEntity.expirationDate,
            createdAt = reliefPackageDbEntity.createdAt,
            balanceBefore = reliefPackageDbEntity.balanceBefore,
            balanceAfter = reliefPackageDbEntity.balanceAfter
        )
    }

    private fun convertForPost(reliefPackage: ReliefPackage): SmartcardDepositApiEntity {
        return SmartcardDepositApiEntity(
            reliefPackageId = reliefPackage.id,
            createdAt = reliefPackage.createdAt,
            smartcardSerialNumber = reliefPackage.tagId,
            balanceBefore = reliefPackage.balanceBefore,
            balanceAfter = reliefPackage.balanceAfter
        )
    }

    companion object {
        private val TAG = DepositRepositoryImpl::class.java.simpleName
        const val PACKAGE_STATE_TO_DISTRIBUTE = "To distribute"
    }
}
