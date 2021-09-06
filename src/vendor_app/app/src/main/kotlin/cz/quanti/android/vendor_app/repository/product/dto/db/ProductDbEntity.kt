package cz.quanti.android.vendor_app.repository.product.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_PRODUCT)
data class ProductDbEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0,
    var name: String = "",
    var image: String = "",
    var unit: String = "",
    var categoryId: Long? = 0,
    var unitPrice: Long? = 0,
    var currency: String? = ""
)
