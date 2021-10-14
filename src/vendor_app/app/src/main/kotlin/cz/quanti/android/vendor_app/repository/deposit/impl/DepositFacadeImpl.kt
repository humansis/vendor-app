package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dto.AssistanceBeneficiary
import cz.quanti.android.vendor_app.repository.deposit.dto.Deposit
import cz.quanti.android.vendor_app.repository.deposit.dto.RemoteDeposit
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import quanti.com.kotlinlog.Log

class DepositFacadeImpl(
    private val depositRepo: DepositRepository
): DepositFacade {

    override fun syncWithServer(vendorId: Int): Completable {
        return sendDataToServer()
            .andThen(loadDataFromServer(vendorId))
    }

    override fun getAssistanceBeneficiaries(): Single<List<AssistanceBeneficiary>> {
        return depositRepo.getAssistanceBeneficiariesFromDB()
    }

    override fun getDeposit(assistanceId: Int, beneficiaryId: Int): Single<Deposit> {
        return depositRepo.getRemoteDepositFromDB(assistanceId).map {
            convert(it, beneficiaryId)
        }
    }

    private fun sendDataToServer(): Completable {
        return depositRepo.getSmartcardDepositsFromDB().flatMapCompletable { deposits ->
            if (deposits.isNotEmpty()) {
                depositRepo.uploadSmartcardDeposits().flatMapCompletable { responseCode ->
                    if (isPositiveResponseHttpCode(responseCode)) {
                        Completable.complete()
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

    private fun loadDataFromServer(vendorId: Int): Completable {
        return depositRepo.downloadRemoteDeposits(vendorId).flatMapCompletable { response ->
            val responseCode = response.first
            when {
                isPositiveResponseHttpCode(responseCode) -> {
                    Observable.fromIterable(response.second).flatMapCompletable {
                        depositRepo.saveRemoteDepositToDB(it).andThen(
                            depositRepo.downloadAssistanceBeneficiaries(it.assistanceId)
                                .flatMapCompletable { abResponse ->
                                    if (isPositiveResponseHttpCode(abResponse.first)) {
                                        Log.d(TAG, "RD sync successful")
                                        depositRepo.saveAssistanceBeneficiesToDB(abResponse.second)
                                    } else {
                                        throw VendorAppException("Could not download RD").apply {
                                            this.apiResponseCode = responseCode
                                            this.apiError = true
                                        }
                                    }
                                }
                        )
                    }
                }
                responseCode == 403 -> {
                    Log.d(TAG, "RD sync denied")
                    Completable.complete()
                }
                else -> {
                    throw VendorAppException("Could not download RD").apply {
                        this.apiResponseCode = responseCode
                        this.apiError = true
                    }
                }
            }
        }
    }

    private fun convert(remoteDeposit: RemoteDeposit, beneficiaryId: Int): Deposit {
        return Deposit(
            beneficiaryId = beneficiaryId,
            depositId = remoteDeposit.assistanceId,
            expirationDate = remoteDeposit.expirationDate,
            limits = mapOf(
                0 to remoteDeposit.foodLimit,
                1 to remoteDeposit.nonfoodLimit,
                2 to remoteDeposit.cashbackLimit
            ),
            amount = remoteDeposit.amount,
            currency = remoteDeposit.currency
        )
    }

    companion object {
        private val TAG = DepositFacadeImpl::class.java.simpleName
    }
}
