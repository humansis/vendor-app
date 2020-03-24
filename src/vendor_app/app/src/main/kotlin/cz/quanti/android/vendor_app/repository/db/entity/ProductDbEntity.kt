package cz.quanti.android.vendor_app.repository.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.db.schema.VendorDb

@Entity(tableName = VendorDb.TABLE_PRODUCT)
data class ProductDbEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0,
    var name: String = "",
    var image: String = "",
    var unit: String = ""
)
