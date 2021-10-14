package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dao.AssistanceBeneficiaryDao
import cz.quanti.android.vendor_app.repository.deposit.dao.RemoteDepositDao
import cz.quanti.android.vendor_app.repository.deposit.dao.SmartcardDepositDao
import cz.quanti.android.vendor_app.repository.deposit.dto.AssistanceBeneficiary
import cz.quanti.android.vendor_app.repository.deposit.dto.RemoteDeposit
import cz.quanti.android.vendor_app.repository.deposit.dto.SmartcardDeposit
import cz.quanti.android.vendor_app.repository.deposit.dto.api.AssistanceBeneficiaryApiEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.api.RemoteDepositApiEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.api.SmartcardDepositApiEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.db.AssistanceBeneficiaryDbEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.db.RemoteDepositDbEntity
import cz.quanti.android.vendor_app.repository.deposit.dto.db.SmartcardDepositDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import quanti.com.kotlinlog.Log

class DepositRepositoryImpl(
    private val remoteDepositDao: RemoteDepositDao,
    private val assistanceBeneficiaryDao: AssistanceBeneficiaryDao,
    private val smartcardDepositDao: SmartcardDepositDao,
    private val api: VendorAPI
) : DepositRepository {

    override fun downloadRemoteDeposits(vendorId: Int): Single<Pair<Int, List<RemoteDeposit>>> {
        return api.getRemoteDeposits(vendorId).map { response ->
            Log.d("xxx", response.message()) // TODO remove
            response.body()?.let { body ->
                Pair(response.code(), body.map {
                    convert(it)
                })
            }
        }
    }

    override fun saveRemoteDepositToDB(remoteDeposit: RemoteDeposit): Completable {
        return remoteDepositDao.insert(convert(remoteDeposit))
    }

    override fun getRemoteDepositFromDB(assistanceId: Int): Single<RemoteDeposit> {
        return remoteDepositDao.getRemoteDepositById(assistanceId).map {
            convert(it)
        }
    }

    override fun downloadAssistanceBeneficiaries(assistanceId: Int): Single<Pair<Int, List<AssistanceBeneficiary>>> {
        return api.getAssistanceBeneficiaries(assistanceId).map { response ->
            response.body()?.let { body ->
                Pair(response.code(), body.map {
                    convert(it)
                })
            }
        }
    }

    override fun saveAssistanceBeneficiesToDB(assistanceBeneficiaries: List<AssistanceBeneficiary>): Completable {
        return Observable.fromIterable(assistanceBeneficiaries).flatMapCompletable {
            saveAssistanceBeneficiaryToDB(it)
        }
    }

    private fun saveAssistanceBeneficiaryToDB(assistanceBeneficiary: AssistanceBeneficiary): Completable {
        return assistanceBeneficiaryDao.insert(convert(assistanceBeneficiary))
    }

    override fun getAssistanceBeneficiariesFromDB(): Single<List<AssistanceBeneficiary>> {
        return assistanceBeneficiaryDao.getAll().map { list->
            list.map {
                convert(it)
            }
        }
    }

    override fun saveSmartcardDepositToDB(): Completable {
        TODO("Not yet implemented")
    }

    override fun getSmartcardDepositsFromDB(): Single<List<SmartcardDeposit>> {
        return smartcardDepositDao.getAll().map { list ->
            list.map { convert(it) }
        }
    }

    override fun uploadSmartcardDeposits(): Single<Int> {
        return api.postDeposit(
            "abcd",
            SmartcardDepositApiEntity(
                1,
                2.0,
                "dneska",
                3,
                1.0,
                3.0
            )
        ).map{ response ->
            response.code()
        }
    }

    private fun convert(remoteDeposit: RemoteDepositApiEntity): RemoteDeposit {
        return RemoteDeposit(
            assistanceId = remoteDeposit.assistanceId,
            dateDistribution = remoteDeposit.dateDistribution,
            expirationDate = remoteDeposit.expirationDate,
            amount = remoteDeposit.amount,
            currency = remoteDeposit.unit,
            foodLimit = remoteDeposit.foodLimit,
            nonfoodLimit = remoteDeposit.nonfoodLimit,
            cashbackLimit = remoteDeposit.cashbackLimit
        )
    }

    private fun convert(remoteDeposit: RemoteDeposit): RemoteDepositDbEntity {
        return RemoteDepositDbEntity(
            assistanceId = remoteDeposit.assistanceId,
            dateDistribution = remoteDeposit.dateDistribution,
            expirationDate = remoteDeposit.expirationDate,
            amount = remoteDeposit.amount,
            currency = remoteDeposit.currency,
            foodLimit = remoteDeposit.foodLimit,
            nonfoodLimit = remoteDeposit.nonfoodLimit,
            cashbackLimit = remoteDeposit.cashbackLimit
        )
    }

    private fun convert(remoteDeposit: RemoteDepositDbEntity): RemoteDeposit {
        return RemoteDeposit(
            assistanceId = remoteDeposit.assistanceId,
            dateDistribution = remoteDeposit.dateDistribution,
            expirationDate = remoteDeposit.expirationDate,
            amount = remoteDeposit.amount,
            currency = remoteDeposit.currency,
            foodLimit = remoteDeposit.foodLimit,
            nonfoodLimit = remoteDeposit.nonfoodLimit,
            cashbackLimit = remoteDeposit.cashbackLimit
        )
    }



    private fun convert(smartcardDepositDbEntity: SmartcardDepositDbEntity): SmartcardDeposit {
        return SmartcardDeposit(
            assistanceId = smartcardDepositDbEntity.assistanceId,
            value = smartcardDepositDbEntity.value,
            createdAt = smartcardDepositDbEntity.createdAt,
            beneficiaryId = smartcardDepositDbEntity.beneficiaryId,
            balanceBefore = smartcardDepositDbEntity.balanceBefore,
            balanceAfter = smartcardDepositDbEntity.balanceAfter
        )
    }

    private fun convert(assistanceBeneficiary: AssistanceBeneficiaryApiEntity): AssistanceBeneficiary {
        TODO("Not yet implemented")
    }

    private fun convert(assistanceBeneficiary: AssistanceBeneficiary): AssistanceBeneficiaryDbEntity {
        TODO("Not yet implemented")
    }

    private fun convert(assistanceBeneficiary: AssistanceBeneficiaryDbEntity): AssistanceBeneficiary{
        return AssistanceBeneficiary(
            id = assistanceBeneficiary.id,
            assistanceId = assistanceBeneficiary.assistanceId,
            beneficiaryId = assistanceBeneficiary.beneficiaryId,
            smartcardSN = assistanceBeneficiary.smartcardSN
        )
    }
}
