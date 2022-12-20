package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dao.ReliefPackageDao
import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
import cz.quanti.android.vendor_app.repository.deposit.dto.api.ReliefPackageApiEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.api.ReliefPackageApiState
import cz.quanti.android.vendor_app.repository.deposit.dto.api.SmartcardDepositApiEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.db.ReliefPackageDbEntity
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.convertHeaderDateToString
import cz.quanti.android.vendor_app.utils.convertStringToDate
import cz.quanti.android.vendor_app.utils.getSerializedName
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date
import quanti.com.kotlinlog.Log
import retrofit2.Response

class DepositRepositoryImpl(
    private val preferences: AppPreferences,
    private val reliefPackageDao: ReliefPackageDao,
    private val api: VendorAPI
) : DepositRepository {

    override fun uploadReliefPackages(): Completable {
        return getDistributedReliefPackages().flatMapCompletable { reliefPackages ->
            if (reliefPackages.isNotEmpty()) {
                postReliefPackages(reliefPackages).flatMapCompletable { response ->
                    if (isPositiveResponseHttpCode(response.code())) {
                        deleteReliefPackagesFromDbById(reliefPackages.map { it.id })
                    } else {
                        throw VendorAppException("Could not upload RD").apply {
                            this.apiResponseCode = response.code()
                            this.apiError = true
                        }
                    }
                }
            } else {
                Log.d(TAG, "No completed RD to upload")
                Completable.complete()
            }
        }
    }

    override fun downloadReliefPackages(
        vendorId: Int
    ): Completable {
        val lastReliefPackageSync = preferences.lastReliefPackageSync
        val toDistribute = if (lastReliefPackageSync == null) {
            ReliefPackageApiState.PACKAGE_STATE_TO_DISTRIBUTE.getSerializedName()
        } else {
            null
        }
        return api.getReliefPackages(vendorId, lastReliefPackageSync, toDistribute)
            .flatMapCompletable { response ->
                when {
                    isPositiveResponseHttpCode(response.code()) -> {
                        response.body()?.data?.let { reliefPackages ->
                            response.headers().get(HEADER_NAME_DATE)?.let { headerDateString ->
                                preferences.lastReliefPackageSync =
                                    convertHeaderDateToString(headerDateString)
                            }
                            actualizeDatabase(reliefPackages, toDistribute != null)
                        }
                    }
                    response.code() == 403 -> {
                        Log.d(TAG, "RD sync denied")
                        preferences.lastReliefPackageSync = null
                        deleteReliefPackagesFromDb()
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

    private fun actualizeDatabase(reliefPackages: List<ReliefPackageApiEntity>, replaceAll: Boolean): Completable {
        val packagesToSave = mutableListOf<ReliefPackageApiEntity>()

        return if (replaceAll) {
            packagesToSave.addAll(reliefPackages)
            deleteReliefPackagesFromDb()
        } else {
            val packagesToDelete = mutableListOf<ReliefPackageApiEntity>()
            reliefPackages.forEach {
                if (it.state == ReliefPackageApiState.PACKAGE_STATE_TO_DISTRIBUTE ||
                    it.state == ReliefPackageApiState.PACKAGE_STATE_DISTRIBUTION_IN_PROGRESS
                ) {
                    packagesToSave.add(it)
                } else {
                    packagesToDelete.add(it)
                }
            }
            deleteReliefPackagesFromDbById(packagesToDelete.map { it.id })
        }.andThen(
            // New packages from packagesToSave will be saved, known packages will be replaced.
            // We should not worry about rewriting distributed packages, as new relief packages
            // aren't downloaded until all distributed packages are uploaded successfully.
            saveReliefPackagesToDb(packagesToSave.map { convert(it) })
        )
    }

    private fun saveReliefPackagesToDb(reliefPackages: List<ReliefPackage>): Completable {
        return Observable.fromIterable(reliefPackages).flatMapCompletable {
            saveReliefPackageToDb(it)
        }
    }

    private fun saveReliefPackageToDb(reliefPackage: ReliefPackage): Completable {
        return reliefPackageDao.insert(convert(reliefPackage))
    }

    override fun getReliefPackagesFromDb(tagId: String): Single<List<ReliefPackage>> {
        return reliefPackageDao.getReliefPackagesByTagId(tagId).map { list ->
            list.map {
                convert(it)
            }
        }
    }

    private fun deleteReliefPackagesFromDb(): Completable {
        return reliefPackageDao.deleteAll()
    }

    private fun deleteReliefPackagesFromDbById(ids: List<Int>): Completable {
        return reliefPackageDao.deleteById(ids)
    }

    override fun deleteOldReliefPackages(): Completable {
        return reliefPackageDao.deleteOlderThan(Date())
    }

    override fun updateReliefPackageInDb(reliefPackage: ReliefPackage): Completable {
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

    private fun postReliefPackages(reliefPackages: List<ReliefPackage>): Single<Response<Unit>> {
        return api.postReliefPackages(reliefPackages.map {
            convertForPost(it)
        })
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
        private const val HEADER_NAME_DATE = "Date"
    }
}
