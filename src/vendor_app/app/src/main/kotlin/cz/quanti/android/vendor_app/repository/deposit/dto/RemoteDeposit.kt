package cz.quanti.android.vendor_app.repository.deposit.dto

class RemoteDeposit(
    val assistanceId: Int,
    val dateDistribution: String,
    val expirationDate: String,
    val amount: Double,
    val currency: String,
    val foodLimit: Double?,
    val nonfoodLimit: Double?,
    val cashbackLimit: Double?
)
