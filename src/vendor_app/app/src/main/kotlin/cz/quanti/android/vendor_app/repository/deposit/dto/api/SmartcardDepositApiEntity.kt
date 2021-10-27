package cz.quanti.android.vendor_app.repository.deposit.dto.api

class SmartcardDepositApiEntity(
    val reliefPackageId: Int,
    val createdAt: String?,
    val smartcardSerialNumber: String,
    val balanceBefore: Double?,
    val balanceAfter: Double?
)
