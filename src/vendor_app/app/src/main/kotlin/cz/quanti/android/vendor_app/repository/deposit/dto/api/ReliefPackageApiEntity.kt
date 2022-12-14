package cz.quanti.android.vendor_app.repository.deposit.dto.api

import com.google.gson.annotations.SerializedName

class ReliefPackageApiEntity(
    val id: Int,
    val state: ReliefPackageApiState,
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

enum class ReliefPackageApiState {
    @SerializedName("To distribute") PACKAGE_STATE_TO_DISTRIBUTE,
    @SerializedName("Distribution in progress") PACKAGE_STATE_DISTRIBUTION_IN_PROGRESS,
    @SerializedName("Distributed") PACKAGE_STATE_DISTRIBUTED,
    @SerializedName("Expired") PACKAGE_STATE_EXPIRED,
    @SerializedName("Canceled") PACKAGE_STATE_CANCELED
}
