package cz.quanti.android.vendor_app.repository.purchase.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_PURCHASE)
data class PurchaseDbEntity(
    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0,
    var createdAt: String = "",
    var vendorId: Long = 0,
    var currency: String = ""
)
