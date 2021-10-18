package cz.quanti.android.vendor_app.repository.deposit.dto.api

import cz.quanti.android.vendor_app.repository.deposit.dto.ReliefPackageState

class SmartcardDepositApiEntity(
    val state: ReliefPackageState,
    val createdAt: String?,
    val balanceBefore: Double?,
    val balanceAfter: Double?
)
