package cz.quanti.android.vendor_app.repository.card.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_BLOCKED_SMARTCARD)
data class BlockedCardDbEntity(
    @PrimaryKey(autoGenerate = false)
    var id: String = ""
)
