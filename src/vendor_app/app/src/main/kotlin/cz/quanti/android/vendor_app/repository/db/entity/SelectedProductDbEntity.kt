package cz.quanti.android.vendor_app.repository.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.db.schema.VendorDb

@Entity(tableName = VendorDb.TABLE_SELECTED_PRODUCT)
data class SelectedProductDbEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var productId: Long = 0,
    var quantity: Long = 0,
    var price: Long = 0
)
