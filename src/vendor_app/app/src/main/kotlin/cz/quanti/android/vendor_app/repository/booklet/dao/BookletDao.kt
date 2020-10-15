package cz.quanti.android.vendor_app.repository.booklet.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet
import cz.quanti.android.vendor_app.repository.booklet.dto.db.BookletDbEntity
import io.reactivex.Single

@Dao
interface BookletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(booklet: BookletDbEntity)

    @Delete
    fun delete(booklet: BookletDbEntity)

    @Query(
        "SELECT * FROM " + VendorDb.TABLE_BOOKLET
            + " WHERE state = " + Booklet.STATE_DEACTIVATED
            + " OR state = " + Booklet.STATE_NEWLY_DEACTIVATED
    )
    fun getAllDeactivated(): Single<List<BookletDbEntity>>

    @Query("SELECT * FROM " + VendorDb.TABLE_BOOKLET + " WHERE state = " + Booklet.STATE_NEWLY_DEACTIVATED)
    fun getNewlyDeactivated(): Single<List<BookletDbEntity>>

    @Query("SELECT * FROM " + VendorDb.TABLE_BOOKLET + " WHERE state = " + Booklet.STATE_PROTECTED)
    fun getProtected(): Single<List<BookletDbEntity>>

    @Query("DELETE FROM " + VendorDb.TABLE_BOOKLET + " WHERE state = " + Booklet.STATE_DEACTIVATED)
    fun deleteDeactivated()

    @Query("DELETE FROM " + VendorDb.TABLE_BOOKLET + " WHERE state = " + Booklet.STATE_PROTECTED)
    fun deleteProtected()

    @Query("DELETE FROM " + VendorDb.TABLE_BOOKLET + " WHERE state = " + Booklet.STATE_NEWLY_DEACTIVATED)
    fun deleteNewlyDeactivated()

    @Query("SELECT count(*) FROM " + VendorDb.TABLE_BOOKLET + " WHERE state = " + Booklet.STATE_NEWLY_DEACTIVATED)
    fun getNewlyDeactivatedCount(): Single<Int>
}
