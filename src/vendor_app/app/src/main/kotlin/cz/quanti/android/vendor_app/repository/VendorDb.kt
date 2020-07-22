package cz.quanti.android.vendor_app.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.quanti.android.vendor_app.repository.booklet.dao.BookletDao
import cz.quanti.android.vendor_app.repository.booklet.dto.db.BookletDbEntity
import cz.quanti.android.vendor_app.repository.card.dao.BlockedCardDao
import cz.quanti.android.vendor_app.repository.card.dto.db.BlockedCardDbEntity
import cz.quanti.android.vendor_app.repository.product.dao.ProductDao
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dao.CardPurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dao.PurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dao.SelectedProductDao
import cz.quanti.android.vendor_app.repository.purchase.dao.VoucherPurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dto.db.CardPurchaseDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.PurchaseDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.SelectedProductDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.VoucherPurchaseDbEntity
import cz.quanti.android.vendor_app.repository.utils.typeconverter.DateTypeConverter

@Database(
    entities = [
        BookletDbEntity::class,
        ProductDbEntity::class,
        SelectedProductDbEntity::class,
        PurchaseDbEntity::class,
        VoucherPurchaseDbEntity::class,
        CardPurchaseDbEntity::class,
        BlockedCardDbEntity::class
    ], version = 2, exportSchema = false
)
@TypeConverters(DateTypeConverter::class)
abstract class VendorDb : RoomDatabase() {
    abstract fun bookletDao(): BookletDao
    abstract fun productDao(): ProductDao
    abstract fun selectedProductDao(): SelectedProductDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun voucherPurchaseDao(): VoucherPurchaseDao
    abstract fun cardPurchaseDao(): CardPurchaseDao
    abstract fun blockedCardDao(): BlockedCardDao

    companion object {
        const val DB_NAME = "cz.quanti.android.pin.vendor_app.database"
        const val TABLE_BOOKLET = "booklet"
        const val TABLE_PRODUCT = "product"
        const val TABLE_SELECTED_PRODUCT = "selected_product"
        const val TABLE_PURCHASE = "purchase"
        const val TABLE_VOUCHER_PURCHASE = "voucher_purchase"
        const val TABLE_CARD_PURCHASE = "card_purchase"
        const val TABLE_BLOCKED_SMARTCARD = "blocked_smartcard"
    }
}
