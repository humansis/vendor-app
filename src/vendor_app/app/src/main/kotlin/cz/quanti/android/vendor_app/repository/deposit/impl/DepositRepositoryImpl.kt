package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dao.ReliefPackageDao
import cz.quanti.android.vendor_app.repository.deposit.dto.*
import cz.quanti.android.vendor_app.repository.deposit.dto.api.ReliefPackageApiEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.api.SmartcardDepositApiEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.db.ReliefPackageDbEntity
import cz.quanti.android.vendor_app.utils.convertStringToDate
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class DepositRepositoryImpl(
    private val reliefPackageDao: ReliefPackageDao,
    private val api: VendorAPI
) : DepositRepository {

    override fun downloadReliefPackages(vendorId: Int): Single<Pair<Int, List<ReliefPackage>>> {
        return api.getReliefPackages(vendorId, PACKAGE_STATE_TO_DISTRIBUTE)
            .map { response ->
                response.body()?.data?.filter {
                    val expirationDate = convertStringToDate(it.expirationDate)
                    expirationDate != null && expirationDate > Date()
                }?.let { data ->
                    Pair(response.code(), data.map {
                        convert(it)
                    })
                }
            }
    }

    override fun saveReliefPackagesToDB(reliefPackages: List<ReliefPackage>): Completable {
        return Observable.fromIterable(reliefPackages).flatMapCompletable {
            saveReliefPackageToDB(it)
        }
    }

    private fun saveReliefPackageToDB(reliefPackage: ReliefPackage): Completable {
        return reliefPackageDao.insert(convert(reliefPackage))
    }

    override fun getReliefPackageFromDB(tagId: String): Single<List<ReliefPackage?>> {
        return reliefPackageDao.getReliefPackagesByTagId(tagId).map { list ->
            list.map { reliefPackage ->
                reliefPackage?.let { convert(it) }
            }
        }
    }

    override fun deleteReliefPackagesFromDB(): Completable {
        return reliefPackageDao.deleteAll()
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

    override fun getDistributedReliefPackages(): Single<List<ReliefPackage>> {
        return reliefPackageDao.getDistributedReliefPackages().map { list ->
            list.map {
                convert(it)
            }
        }
    }

    override fun postReliefPackages(reliefPackages: List<ReliefPackage>): Single<Int> {
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
            expirationDate = reliefPackageApiEntity.expirationDate
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
        const val PACKAGE_STATE_TO_DISTRIBUTE = "To distribute"
    }
}
