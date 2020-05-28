package cz.quanti.android.vendor_app.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.quanti.android.vendor_app.repository.card.dao.CardPaymentDao
import cz.quanti.android.vendor_app.repository.card.dto.db.CardPaymentDbEntity
import cz.quanti.android.vendor_app.repository.product.dao.ProductDao
import cz.quanti.android.vendor_app.repository.product.dao.SelectedProductDao
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import cz.quanti.android.vendor_app.repository.product.dto.db.SelectedProductDbEntity
import cz.quanti.android.vendor_app.repository.utils.typeconverter.DateTypeConverter
import cz.quanti.android.vendor_app.repository.utils.typeconverter.ProductIdListTypeConverter
import cz.quanti.android.vendor_app.repository.voucher.dao.BookletDao
import cz.quanti.android.vendor_app.repository.voucher.dao.VoucherDao
import cz.quanti.android.vendor_app.repository.voucher.dto.db.BookletDbEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.db.VoucherDbEntity

@Database(
    entities = [
        BookletDbEntity::class,
        ProductDbEntity::class,
        VoucherDbEntity::class,
        SelectedProductDbEntity::class,
        CardPaymentDbEntity::class
    ], version = 2, exportSchema = false
)
@TypeConverters(ProductIdListTypeConverter::class, DateTypeConverter::class)
abstract class VendorDb : RoomDatabase() {
    abstract fun bookletDao(): BookletDao
    abstract fun productDao(): ProductDao
    abstract fun voucherDao(): VoucherDao
    abstract fun selectedProductDao(): SelectedProductDao
    abstract fun cardPaymentDao(): CardPaymentDao

    companion object {
        const val DB_NAME = "cz.quanti.android.pin.vendor_app.database"
        const val TABLE_BOOKLET = "booklet"
        const val TABLE_PRODUCT = "product"
        const val TABLE_VOUCHER = "voucher"
        const val TABLE_SELECTED_PRODUCT = "selected_product"
        const val TABLE_CARD_PAYMENT = "card_payment"
    }
}
