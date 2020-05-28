package cz.quanti.android.vendor_app.repository.card.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_CARD_PAYMENT)
data class CardPaymentDbEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var cardId: String = "",
    var productId: Long = 0,
    var value: Double = 0.0,
    var quantity: Double = 0.0,
    var createdAt: String = ""
)
