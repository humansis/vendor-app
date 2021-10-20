package cz.quanti.android.vendor_app.repository.deposit.dto

class ReliefPackage (
    val id: Int,
    val assistanceId: Int,
    val beneficiaryId: Int,
    val amount: Double,
    val currency: String,
    val tagId: String,
    val foodLimit: Double?,
    val nonfoodLimit: Double?,
    val cashbackLimit: Double?,
    val expirationDate: String,
    var createdAt: String? = null,
    var balanceBefore: Double? = null,
    var balanceAfter: Double? = null
) {

}
