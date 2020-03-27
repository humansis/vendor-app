package cz.quanti.android.vendor_app.repository.product.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import cz.quanti.android.vendor_app.repository.product.dto.db.SelectedProductDbEntity
import io.reactivex.Single

@Dao
interface SelectedProductDao {
    @Insert
    fun insert(product: SelectedProductDbEntity): Single<Long>

    @Delete
    fun delete(product: SelectedProductDbEntity)
}
