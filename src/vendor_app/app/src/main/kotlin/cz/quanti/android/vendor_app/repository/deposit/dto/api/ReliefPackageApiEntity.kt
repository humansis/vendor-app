package cz.quanti.android.vendor_app.repository.deposit.dto.api

class ReliefPackageApiEntity(
    val id: Int,
    val assistanceId: Int,
    val beneficiaryId: Int,
    val amountToDistribute: Double,
    val unit: String,
    val smartCardSerialNumber: String,
    val foodLimit: Double?,
    val nonfoodLimit: Double?,
    val cashbackLimit: Double?,
    val expirationDate: String?
)
