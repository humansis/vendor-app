package cz.quanti.android.vendor_app.repository.invoice.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.quanti.android.vendor_app.repository.VendorDb

@Entity(tableName = VendorDb.TABLE_INVOICE)
data class InvoiceDbEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0,
    var date: String = "",
    var quantity: Int = 0,
    var value: Double = 0.0,
    var currency: String = ""
)
