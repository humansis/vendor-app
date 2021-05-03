package cz.quanti.android.vendor_app.repository.purchase.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_TRANSACTION_PURCHASE)
data class TransactionPurchaseDbEntity(
    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0,
    var value: Long = 0,
    var currency: String = "",
    var beneficiaryId: Long = 0,
    var createdAt: String = "",
    var transactionId: Long = 0
)
