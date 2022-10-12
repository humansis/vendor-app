package cz.quanti.android.vendor_app.repository.purchase.dto.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(
    tableName = VendorDb.TABLE_PURCHASED_PRODUCT,
    indices = [
        androidx.room.Index(
            value = ["purchaseId"]
        )
    ],
    foreignKeys = [
        ForeignKey(
            onDelete = ForeignKey.CASCADE,
            entity = PurchaseDbEntity::class,
            parentColumns = ["dbId"],
            childColumns = ["purchaseId"]
        )
    ]
)
data class PurchasedProductDbEntity(
    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0,
    var productId: Long = 0,
    var value: Double = 0.0,
    var purchaseId: Long = 0
)
