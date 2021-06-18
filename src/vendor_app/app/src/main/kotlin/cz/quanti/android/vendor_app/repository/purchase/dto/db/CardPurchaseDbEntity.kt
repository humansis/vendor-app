package cz.quanti.android.vendor_app.repository.purchase.dto.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(
    tableName = VendorDb.TABLE_CARD_PURCHASE,
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = PurchaseDbEntity::class,
        parentColumns = ["dbId"],
        childColumns = ["purchaseId"]
    )]
)
data class CardPurchaseDbEntity(
    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0,
    var card: String? = "",
    var purchaseId: Long = 0
)
