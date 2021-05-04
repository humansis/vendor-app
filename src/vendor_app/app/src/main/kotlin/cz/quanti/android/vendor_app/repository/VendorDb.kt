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
import cz.quanti.android.vendor_app.repository.purchase.dao.*
import cz.quanti.android.vendor_app.repository.purchase.dto.api.TransactionPurchaseApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.*
import cz.quanti.android.vendor_app.repository.utils.typeconverter.DateTypeConverter

@Database(
    entities = [
        BookletDbEntity::class,
        ProductDbEntity::class,
        SelectedProductDbEntity::class,
        PurchaseDbEntity::class,
        VoucherPurchaseDbEntity::class,
        CardPurchaseDbEntity::class,
        BlockedCardDbEntity::class,
        InvoiceDbEntity::class,
        TransactionDbEntity::class,
        TransactionPurchaseDbEntity::class
    ], version = 3, exportSchema = false
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
    abstract fun invoiceDao(): InvoiceDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionPurchaseDao(): TransactionPurchaseDao

    companion object {
        const val DB_NAME = "cz.quanti.android.pin.vendor_app.database"
        const val TABLE_BOOKLET = "booklet"
        const val TABLE_PRODUCT = "product"
        const val TABLE_SELECTED_PRODUCT = "selected_product"
        const val TABLE_PURCHASE = "purchase"
        const val TABLE_VOUCHER_PURCHASE = "voucher_purchase"
        const val TABLE_CARD_PURCHASE = "card_purchase"
        const val TABLE_BLOCKED_SMARTCARD = "blocked_smartcard"
        const val TABLE_INVOICE = "invoice"
        const val TABLE_TRANSACTION_BATCH = "transaction_batch"
        const val TABLE_TRANSACTION_PURCHASE = "transaction_purchase"
    }
}
