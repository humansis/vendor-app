package cz.quanti.android.vendor_app.repository.deposit.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_ASSISTANCE_BENEFICIARY)
class AssistanceBeneficiaryDbEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val assistanceId: Long,
    val beneficiaryId: Long,
    val smartcardSN: String
)
