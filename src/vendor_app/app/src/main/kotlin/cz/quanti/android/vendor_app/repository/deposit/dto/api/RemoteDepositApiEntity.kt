package cz.quanti.android.vendor_app.repository.deposit.dto.api

class RemoteDepositApiEntity(
    val assistanceId: Int,
    val dateDistribution: String,
    val expirationDate: String,
    val amount: Double,
    val unit: String,
    val foodLimit: Double?,
    val nonfoodLimit: Double?,
    val cashbackLimit: Double?
)
