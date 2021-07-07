package cz.quanti.android.vendor_app.repository.booklet.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet

@Entity(tableName = VendorDb.TABLE_BOOKLET)
data class BookletDbEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var code: String = "",
    var password: String = "",
    var state: Int = Booklet.STATE_NORMAL
)
