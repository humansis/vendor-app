package cz.quanti.android.vendor_app.repository.deposit.dao

import androidx.room.*
import cz.quanti.android.vendor_app.repository.VendorDb
import cz.quanti.android.vendor_app.repository.deposit.dto.db.ReliefPackageDbEntity
import io.reactivex.Completable
import io.reactivex.Single
import java.util.Date

@Dao
interface ReliefPackageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reliefPackage: ReliefPackageDbEntity): Completable

    @Query("UPDATE " + VendorDb.TABLE_RELIEF_PACKAGE + " SET createdAt = :createdAt, balanceBefore = :balanceBefore, balanceAfter = :balanceAfter WHERE id = :id")
    fun update(id: Int, createdAt: String?, balanceBefore: Double?, balanceAfter: Double?): Completable

    @Delete
    fun delete(reliefPackage: ReliefPackageDbEntity): Completable

    @Query("DELETE FROM " + VendorDb.TABLE_RELIEF_PACKAGE)
    fun deleteAll(): Completable

    @Query("DELETE FROM " + VendorDb.TABLE_RELIEF_PACKAGE + " WHERE expirationDate < :date ")
    fun deleteOlderThan(date: Date): Completable

    @Query("SELECT * FROM " + VendorDb.TABLE_RELIEF_PACKAGE)
    fun getAll(): Single<List<ReliefPackageDbEntity>>

    @Query("SELECT * FROM " + VendorDb.TABLE_RELIEF_PACKAGE + " WHERE createdAt IS NOT NULL")
    fun getDistributedReliefPackages() :Single<List<ReliefPackageDbEntity>>

    @Query("SELECT * FROM " + VendorDb.TABLE_RELIEF_PACKAGE + " WHERE id = :reliefPackageId")
    fun getReliefPackageById(reliefPackageId: Int): Single<ReliefPackageDbEntity?>

    @Query("SELECT * FROM " + VendorDb.TABLE_RELIEF_PACKAGE + " WHERE tagId = :tagId")
    fun getReliefPackagesByTagId(tagId: String): Single<List<ReliefPackageDbEntity>>
}
