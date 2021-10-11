package cz.quanti.android.vendor_app.repository.deposit.dto

class SmartcardDeposit(
    val assistanceId: Long,
    val value: Double,
    val createdAt: String,
    val beneficiaryId: Long,
    val balanceBefore: Double,
    val balanceAfter: Double
)
