package cz.quanti.android.vendor_app.repository.card.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.card.dto.db.CardPaymentDbEntity
import io.reactivex.Single

@Dao
interface CardPaymentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(cardPayment: CardPaymentDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_CARD_PAYMENT)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_CARD_PAYMENT)
    fun getAll(): Single<List<CardPaymentDbEntity>>
}
