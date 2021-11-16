package cz.quanti.android.vendor_app.repository.deposit.dto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.utils.typeconverter.DateTypeConverter
import java.util.Date

@Entity(tableName = VendorDb.TABLE_RELIEF_PACKAGE)
class ReliefPackageDbEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val assistanceId: Int,
    val beneficiaryId: Int,
    val amount: Double,
    val currency: String,
    val tagId: String,
    val foodLimit: Double?,
    val nonfoodLimit: Double?,
    val cashbackLimit: Double?,
    @TypeConverters(DateTypeConverter::class)
    val expirationDate: Date?,
    val createdAt: String? = null,
    val balanceBefore: Double? = null,
    val balanceAfter: Double? = null
)
