package cz.quanti.android.vendor_app.repository.deposit.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_REMOTE_DEPOSIT)
class RemoteDepositDbEntity(
    @PrimaryKey(autoGenerate = false)
    val assistanceId: Int,
    val dateDistribution: String,
    val expirationDate: String,
    val amount: Double,
    val currency: String,
    val foodLimit: Double?,
    val nonfoodLimit: Double?,
    val cashbackLimit: Double?
)
