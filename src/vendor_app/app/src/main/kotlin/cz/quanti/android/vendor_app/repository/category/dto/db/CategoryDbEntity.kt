package cz.quanti.android.vendor_app.repository.category.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType

@Entity(tableName = VendorDb.TABLE_CATEGORY)
class CategoryDbEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0,
    var name: String = "",
    var type: String = "",
    var image: String = ""
)
