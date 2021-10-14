package cz.quanti.android.vendor_app.repository.deposit

import cz.quanti.android.vendor_app.repository.deposit.dto.AssistanceBeneficiary
import cz.quanti.android.vendor_app.repository.deposit.dto.Deposit
import cz.quanti.android.vendor_app.repository.deposit.dto.RemoteDeposit
import io.reactivex.Completable
import io.reactivex.Single

interface DepositFacade {
    fun syncWithServer(vendorId: Int): Completable

    fun getAssistanceBeneficiaries(): Single<List<AssistanceBeneficiary>>

    fun getDeposit(assistanceId: Int, beneficiaryId: Int): Single<Deposit>
}
