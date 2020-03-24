package cz.quanti.android.vendor_app.repository.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.db.schema.VendorDb

@Entity(tableName = VendorDb.TABLE_BOOKLET)
data class BookletDbEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0,
    var code: String = ""
)
