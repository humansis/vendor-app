package cz.quanti.android.vendor_app.repository.deposit.dto

import java.util.*

class Deposit(
    val beneficiaryId: Int,
    val depositId: Int,
    val expirationDate: Date?,
    val limits: Map<Int, Double?>,
    val amount: Double,
    val currency: String
)
