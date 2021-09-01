package cz.quanti.android.vendor_app.repository.transaction.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_TRANSACTION_BATCH)
data class TransactionDbEntity(
    @PrimaryKey(autoGenerate = false)
    var dbId: Long = 0,
    var projectId: Long = 0,
    var value: Double = 0.0,
    var currency: String = ""
)
