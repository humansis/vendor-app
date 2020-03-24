package cz.quanti.android.vendor_app.repository.db.schema

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.quanti.android.vendor_app.repository.db.dao.BookletDao
import cz.quanti.android.vendor_app.repository.db.dao.ProductDao
import cz.quanti.android.vendor_app.repository.db.dao.SelectedProductDao
import cz.quanti.android.vendor_app.repository.db.dao.VoucherDao
import cz.quanti.android.vendor_app.repository.db.entity.BookletDbEntity
import cz.quanti.android.vendor_app.repository.db.entity.ProductDbEntity
import cz.quanti.android.vendor_app.repository.db.entity.SelectedProductDbEntity
import cz.quanti.android.vendor_app.repository.db.entity.VoucherDbEntity
import cz.quanti.android.vendor_app.repository.db.entity.typeconverter.DateTypeConverter
import cz.quanti.android.vendor_app.repository.db.entity.typeconverter.ProductIdListTypeConverter
import cz.quanti.android.vendor_app.repository.entity.Booklet
import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.Voucher

@Database(
    entities = [
        BookletDbEntity::class,
        ProductDbEntity::class,
        VoucherDbEntity::class,
        SelectedProductDbEntity::class
    ], version = 1, exportSchema = false
)
@TypeConverters(ProductIdListTypeConverter::class, DateTypeConverter::class)
abstract class VendorDb : RoomDatabase() {
    abstract fun bookletDao(): BookletDao
    abstract fun productDao(): ProductDao
    abstract fun voucherDao(): VoucherDao
    abstract fun selectedProductDao(): SelectedProductDao

    companion object {
        const val DB_NAME = "cz.quanti.android.pin.vendor_app.database"
        const val TABLE_BOOKLET = "booklet"
        const val TABLE_PRODUCT = "product"
        const val TABLE_VOUCHER = "voucher"
        const val TABLE_SELECTED_PRODUCT = "selected_product"
    }
}
