package cz.quanti.android.vendor_app.repository.deposit.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.deposit.dto.db.SmartcardDepositDbEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface SmartcardDepositDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(smartcardDeposit: SmartcardDepositDbEntity): Completable

    @Delete
    fun delete(smartcardDeposit: SmartcardDepositDbEntity): Completable

    @Query("DELETE FROM " + VendorDb.TABLE_SMARTCARD_DEPOSIT)
    fun deleteAll(): Completable

    @Query("SELECT * FROM " + VendorDb.TABLE_SMARTCARD_DEPOSIT)
    fun getAll(): Single<List<SmartcardDepositDbEntity>>

    @Query("SELECT * FROM " + VendorDb.TABLE_SMARTCARD_DEPOSIT + " WHERE dbId = :smartcardDepositId")
    fun getSmartcardDepositById(smartcardDepositId: Long): Single<SmartcardDepositDbEntity>
}
