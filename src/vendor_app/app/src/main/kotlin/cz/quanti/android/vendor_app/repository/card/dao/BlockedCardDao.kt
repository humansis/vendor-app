package cz.quanti.android.vendor_app.repository.card.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.card.dto.db.BlockedCardDbEntity
import io.reactivex.Single

@Dao
interface BlockedCardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(card: BlockedCardDbEntity)

    @Query("SELECT * FROM " + VendorDb.TABLE_BLOCKED_SMARTCARD)
    fun getAll(): Single<List<BlockedCardDbEntity>>

    @Query("DELETE FROM " + VendorDb.TABLE_BLOCKED_SMARTCARD)
    fun deleteAll()
}
