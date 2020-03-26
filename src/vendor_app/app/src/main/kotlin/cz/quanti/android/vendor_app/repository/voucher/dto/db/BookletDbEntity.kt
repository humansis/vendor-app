package cz.quanti.android.vendor_app.repository.voucher.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_BOOKLET)
data class BookletDbEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0,
    var code: String = ""
)
