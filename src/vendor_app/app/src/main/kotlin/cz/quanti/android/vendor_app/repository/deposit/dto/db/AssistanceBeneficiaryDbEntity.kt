package cz.quanti.android.vendor_app.repository.deposit.dto.db

import androidx.room.Entity
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_ASSISTANCE_BENEFICIARY)
class AssistanceBeneficiaryDbEntity(
    val id: Long,
    val beneficiaryId: Long,
    val smartcardSN: String
)
