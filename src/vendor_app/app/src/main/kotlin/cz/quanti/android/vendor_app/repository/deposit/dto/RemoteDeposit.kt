package cz.quanti.android.vendor_app.repository.deposit.dto

class RemoteDeposit(
    val assistanceId: Long,
    val dateDistribution: String,
    val expirationDate: String,
    val foodLimit: Double?,
    val nonfoodLimit: Double?,
    val cashbackLimit: Double?
)
