package cz.quanti.android.vendor_app.repository.deposit.dto

import cz.quanti.android.nfc.dto.v2.Deposit
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.utils.convertStringToDate
import java.util.*

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

    fun convertToDeposit(): Deposit {
        return Deposit(
            beneficiaryId = this.beneficiaryId,
            depositId = this.assistanceId,
            expirationDate = convertStringToDate(this.expirationDate),
            limits = getLimits(this),
            amount = this.amount,
            currency = this.currency
        )
    }

    private fun getLimits(reliefPackage: ReliefPackage): Map<Int,Double> {
        val limits = mutableMapOf<Int, Double>()
        reliefPackage.foodLimit?.let {
            limits[CategoryType.FOOD.typeId] = it
        }
        reliefPackage.nonfoodLimit?.let {
            limits[CategoryType.NONFOOD.typeId] = it
        }
        reliefPackage.cashbackLimit?.let {
            limits[CategoryType.CASHBACK.typeId] = it
        }
        return limits
    }
}
