package cz.quanti.android.vendor_app.repository.deposit.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.deposit.dto.db.AssistanceBeneficiaryDbEntity
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface AssistanceBeneficiaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(assistanceBeneficiary: AssistanceBeneficiaryDbEntity): Completable

    @Delete
    fun delete(assistanceBeneficiary: AssistanceBeneficiaryDbEntity): Completable

    @Query("DELETE FROM " + VendorDb.TABLE_ASSISTANCE_BENEFICIARY)
    fun deleteAll(): Completable

    @Query("SELECT * FROM " + VendorDb.TABLE_ASSISTANCE_BENEFICIARY)
    fun getAll(): Observable<List<AssistanceBeneficiaryDbEntity>>

    @Query("SELECT * FROM " + VendorDb.TABLE_ASSISTANCE_BENEFICIARY + " WHERE id = :assistanceBeneficiaryId")
    fun getAssistanceBeneficiaryById(assistanceBeneficiaryId: Long): AssistanceBeneficiaryDbEntity
}
