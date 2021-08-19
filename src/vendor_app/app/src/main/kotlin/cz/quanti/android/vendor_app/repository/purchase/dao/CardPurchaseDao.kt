package cz.quanti.android.vendor_app.repository.purchase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.purchase.dto.db.CardPurchaseDbEntity
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface CardPurchaseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(cardPurchase: CardPurchaseDbEntity)

    @Query("DELETE FROM " + VendorDb.TABLE_CARD_PURCHASE)
    fun deleteAll()

    @Query("SELECT * FROM " + VendorDb.TABLE_CARD_PURCHASE)
    fun getAll(): Single<List<CardPurchaseDbEntity>>

    @Query(
        "SELECT * FROM " + VendorDb.TABLE_CARD_PURCHASE +
            " WHERE purchaseId = :purchaseId"
    )
    fun getCardForPurchase(purchaseId: Long): Maybe<CardPurchaseDbEntity>

    @Query(
        "DELETE FROM " + VendorDb.TABLE_CARD_PURCHASE +
            " WHERE purchaseId = :purchaseId"
    )
    fun deleteCardForPurchase(purchaseId: Long)
}
