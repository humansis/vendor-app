package cz.quanti.android.vendor_app.repository.deposit.dto

import com.google.gson.annotations.SerializedName

enum class ReliefPackageState {
    @SerializedName("To distribute") TO_DISTRIBUTE,
    @SerializedName("Distribution in progress") DISTRIBUTION_IN_PROGRESS,
    @SerializedName("Distributed") DISTRIBUTED,
    @SerializedName("Expired") EXPIRED,
    @SerializedName("Canceled") CANCELED
}
