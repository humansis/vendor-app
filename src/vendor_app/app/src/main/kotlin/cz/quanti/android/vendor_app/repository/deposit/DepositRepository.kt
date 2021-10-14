package cz.quanti.android.vendor_app.repository.deposit

import cz.quanti.android.vendor_app.repository.deposit.dto.AssistanceBeneficiary
import cz.quanti.android.vendor_app.repository.deposit.dto.RemoteDeposit
import cz.quanti.android.vendor_app.repository.deposit.dto.SmartcardDeposit
import io.reactivex.Completable
import io.reactivex.Single

interface DepositRepository {

    fun downloadRemoteDeposits(vendorId: Int): Single<Pair<Int,List<RemoteDeposit>>>

    fun saveRemoteDepositToDB(remoteDeposit: RemoteDeposit): Completable

    fun getRemoteDepositFromDB(assistanceId: Int): Single<RemoteDeposit>

    fun downloadAssistanceBeneficiaries(assistanceId: Int): Single<Pair<Int, List<AssistanceBeneficiary>>>

    fun saveAssistanceBeneficiesToDB(assistanceBeneficiaries: List<AssistanceBeneficiary>): Completable

    fun getAssistanceBeneficiariesFromDB(): Single<List<AssistanceBeneficiary>>

    fun saveSmartcardDepositToDB(): Completable

    fun getSmartcardDepositsFromDB(): Single<List<SmartcardDeposit>>

    fun uploadSmartcardDeposits(): Single<Int>
}
