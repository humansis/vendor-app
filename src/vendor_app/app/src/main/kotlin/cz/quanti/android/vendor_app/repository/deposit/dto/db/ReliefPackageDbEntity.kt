package cz.quanti.android.vendor_app.repository.deposit.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_RELIEF_PACKAGE)
class ReliefPackageDbEntity (
    @PrimaryKey(autoGenerate = false)
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
    val createdAt: String? = null,
    val balanceBefore: Double? = null,
    val balanceAfter: Double? = null
)
