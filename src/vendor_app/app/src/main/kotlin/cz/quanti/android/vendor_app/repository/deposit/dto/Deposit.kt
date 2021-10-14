package cz.quanti.android.vendor_app.repository.deposit.dto

class Deposit(
    val beneficiaryId: Int,
    val depositId: Int,
    val expirationDate: String,
    val limits: Map<Int, Double?>,
    val amount: Double,
    val currency: String
)
