package cz.quanti.android.vendor_app.repository.deposit.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.deposit.dto.db.RemoteDepositDbEntity
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface RemoteDepositDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(remoteDeposit: RemoteDepositDbEntity): Completable

    @Delete
    fun delete(remoteDeposit: RemoteDepositDbEntity): Completable

    @Query("DELETE FROM " + VendorDb.TABLE_REMOTE_DEPOSIT)
    fun deleteAll(): Completable

    @Query("SELECT * FROM " + VendorDb.TABLE_REMOTE_DEPOSIT)
    fun getAll(): Observable<List<RemoteDepositDbEntity>>

    @Query("SELECT * FROM " + VendorDb.TABLE_REMOTE_DEPOSIT + " WHERE assistanceId = :remoteDepositId")
    fun getRemoteDepositById(remoteDepositId: Long): RemoteDepositDbEntity
}
