package cz.quanti.android.vendor_app.repository.deposit.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.deposit.DepositRepository
import cz.quanti.android.vendor_app.repository.deposit.dao.AssistanceBeneficiaryDao
import cz.quanti.android.vendor_app.repository.deposit.dao.RemoteDepositDao
import cz.quanti.android.vendor_app.repository.deposit.dao.SmartcardDepositDao
import cz.quanti.android.vendor_app.repository.deposit.dto.api.RemoteDepositApiEntity
import io.reactivex.Completable
import quanti.com.kotlinlog.Log

class DepositRepositoryImpl(
    private val remoteDepositDao: RemoteDepositDao,
    private val assistanceBeneficiaryDao: AssistanceBeneficiaryDao,
    private val smartcardDepositDao: SmartcardDepositDao,
    private val api: VendorAPI
) : DepositRepository {

    override fun uploadDeposits(): Completable {
        TODO("Not yet implemented")
    }

    override fun downloadDeposits(vendorId: Int): Completable {
        return Completable.fromSingle<List<RemoteDepositApiEntity>> {
            api.getRemoteDeposits(vendorId).map {
                Log.d("xxx", it.message())
            }
        }
    }
}
