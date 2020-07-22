package cz.quanti.android.vendor_app.repository.purchase.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_CARD_PURCHASE)
data class CardPurchaseDbEntity(
    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0,
    var card: String? = "",
    var purchaseId: Long = 0
)
