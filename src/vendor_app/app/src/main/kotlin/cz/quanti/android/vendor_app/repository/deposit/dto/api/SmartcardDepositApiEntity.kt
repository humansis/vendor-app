package cz.quanti.android.vendor_app.repository.deposit.dto.api

class SmartcardDepositApiEntity(
    val assistanceId: Long,
    val value: Double,
    val createdAt: String,
    val beneficiaryId: Long,
    val balanceBefore: Double,
    val balanceAfter: Double
)
