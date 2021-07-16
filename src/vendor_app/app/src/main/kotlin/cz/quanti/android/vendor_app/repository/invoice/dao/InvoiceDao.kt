package cz.quanti.android.vendor_app.repository.invoice.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.invoice.dto.db.InvoiceDbEntity
import io.reactivex.Single

@Dao
interface InvoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(invoice: InvoiceDbEntity): Long

    @Delete
    fun delete(invoice: InvoiceDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_INVOICE)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_INVOICE)
    fun getAll(): Single<List<InvoiceDbEntity>>

    @Query("SELECT count(*) FROM " + VendorDb.TABLE_INVOICE)
    fun getCount(): Single<Int>
}
