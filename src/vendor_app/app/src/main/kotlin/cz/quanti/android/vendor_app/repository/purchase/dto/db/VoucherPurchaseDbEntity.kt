package cz.quanti.android.vendor_app.repository.purchase.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_VOUCHER_PURCHASE)
data class VoucherPurchaseDbEntity(
    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0,
    var voucher: Long = 0,
    var purchaseId: Long = 0
)
