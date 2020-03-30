package cz.quanti.android.vendor_app.repository.voucher.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.db.BookletDbEntity
import io.reactivex.Single

@Dao
interface BookletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(booklet: BookletDbEntity)

    @Delete
    fun delete(booklet: BookletDbEntity)

    @Query("SELECT * FROM " + VendorDb.TABLE_BOOKLET + " WHERE state = " + Booklet.STATE_DEACTIVATED)
    fun getDeactivated(): Single<List<BookletDbEntity>>
}
