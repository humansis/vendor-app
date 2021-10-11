package cz.quanti.android.vendor_app.repository.deposit.dto.db

import androidx.room.Entity
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_REMOTE_DEPOSIT)
class RemoteDepositDbEntity(
    val assistanceId: Long,
    val dateDistribution: String,
    val expirationDate: String,
    val foodLimit: Double,
    val nonfoodLimit: Double,
    val cashbackLimit: Double
)
