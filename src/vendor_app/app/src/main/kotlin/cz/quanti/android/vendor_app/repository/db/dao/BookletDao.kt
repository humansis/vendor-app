package cz.quanti.android.vendor_app.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import cz.quanti.android.vendor_app.repository.db.entity.BookletDbEntity

@Dao
interface BookletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(booklet: BookletDbEntity)

    @Delete
    fun delete(booklet: BookletDbEntity)
}
