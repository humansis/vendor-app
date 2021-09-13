package cz.quanti.android.vendor_app.repository.purchase.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_SELECTED_PRODUCT)
data class SelectedProductDbEntity(
    @PrimaryKey(autoGenerate = true)
    var dbId: Long = 0,
    var productId: Long = 0,
    var value: Double = 0.0,
    var categoryId: Long = 0,
    var currency: String? = ""
)
