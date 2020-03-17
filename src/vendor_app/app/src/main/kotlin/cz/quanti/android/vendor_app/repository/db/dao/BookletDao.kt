package cz.quanti.android.vendor_app.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import cz.quanti.android.vendor_app.repository.entity.Booklet

@Dao
interface BookletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(booklet: Booklet)

    @Delete
    fun delete(booklet: Booklet)
}
