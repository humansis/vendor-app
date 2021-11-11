package cz.quanti.android.vendor_app.repository.category.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.category.dto.db.CategoryDbEntity

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: CategoryDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_CATEGORY)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_CATEGORY + " WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): CategoryDbEntity
}
