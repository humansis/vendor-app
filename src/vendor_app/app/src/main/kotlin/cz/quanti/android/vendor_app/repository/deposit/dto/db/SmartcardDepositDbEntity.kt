package cz.quanti.android.vendor_app.repository.deposit.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_SMARTCARD_DEPOSIT)
class SmartcardDepositDbEntity(
    @PrimaryKey(autoGenerate = true)
    val dbId: Long,
    val assistanceId: Long,
    val value: Double,
    val createdAt: String,
    val beneficiaryId: Long,
    val balanceBefore: Double,
    val balanceAfter: Double
)
