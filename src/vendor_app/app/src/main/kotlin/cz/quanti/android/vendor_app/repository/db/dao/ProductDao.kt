package cz.quanti.android.vendor_app.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import cz.quanti.android.vendor_app.repository.entity.Booklet
import cz.quanti.android.vendor_app.repository.entity.Product

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: Product)

    @Delete
    fun delete(product: Product)
}
