package cz.quanti.android.vendor_app.repository.db.schema

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.quanti.android.vendor_app.repository.db.dao.BookletDao
import cz.quanti.android.vendor_app.repository.db.dao.ProductDao
import cz.quanti.android.vendor_app.repository.db.dao.VoucherDao
import cz.quanti.android.vendor_app.repository.entity.Booklet
import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.Voucher

@Database(
    entities = [
        Booklet::class,
        Product::class,
        Voucher::class
    ], version = 1, exportSchema = false
)
abstract class VendorDb : RoomDatabase() {
    abstract fun bookletDao(): BookletDao
    abstract fun productDao(): ProductDao
    abstract fun voucherDao(): VoucherDao

    companion object {
        const val DB_NAME = "cz.quanti.android.pin.vendor_app.database"
        const val TABLE_BOOKLET = "booklet"
        const val TABLE_PRODUCT = "product"
        const val TABLE_VOUCHER = "voucher"
    }
}
