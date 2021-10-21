package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackage
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

    override fun getDepositByTag(tagId: String): Single<List<ReliefPackage?>> {
        return depositRepo.getReliefPackageFromDB(tagId)
    }

    override fun deleteReliefPackageFromDB(id: Int): Completable {
        return depositRepo.deleteReliefPackageFromDB(id)
    }

    override fun updateReliefPackageInDB(reliefPackage: ReliefPackage): Completable {
        return depositRepo.updateReliefPackageInDB(reliefPackage)
    }

    private fun sendDataToServer(): Completable {
        return depositRepo.getDistributedReliefPackages().flatMapCompletable { deposits ->
            if (deposits.isNotEmpty()) {
                Observable.fromIterable(deposits).flatMapCompletable { reliefPackage ->
                    depositRepo.patchReliefPackage(reliefPackage).flatMapCompletable { responseCode ->
                        if (isPositiveResponseHttpCode(responseCode)) {
                            depositRepo.deleteReliefPackageFromDB(reliefPackage.id)
                        } else {
                            throw VendorAppException("Could not upload RD").apply {
                                this.apiResponseCode = responseCode
                                this.apiError = true
                            }
                        }
                    }
                }
            } else {
                Completable.complete()
            }
        }
    }

    private fun loadDataFromServer(vendorId: Int): Completable {
        return depositRepo.downloadReliefPackages(vendorId).flatMapCompletable { response ->
            val responseCode = response.first
            when {
                isPositiveResponseHttpCode(responseCode) -> {
                    if (response.second.isEmpty()) {
                        throw VendorAppException("RD returned from server were empty.")
                    } else {
                        actualizeDatabase(response.second)
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

    private fun actualizeDatabase(reliefPackages: List<ReliefPackage>): Completable {
        return depositRepo.deleteReliefPackagesFromDB().andThen(
            depositRepo.saveReliefPackagesToDB(reliefPackages)
        )
    }

    companion object {
        private val TAG = DepositFacadeImpl::class.java.simpleName
    }
}
