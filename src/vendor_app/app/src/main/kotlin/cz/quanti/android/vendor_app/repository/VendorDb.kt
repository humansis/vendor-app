package cz.quanti.android.vendor_app.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

        val MIGRATION_2_3 = object : Migration(2,3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE 'invoice' ('id' INTEGER NOT NULL, 'date' TEXT NOT NULL, 'quantity' INTEGER NOT NULL, 'value' REAL NOT NULL, 'currency' TEXT NOT NULL, PRIMARY KEY('id'))")
                database.execSQL("CREATE TABLE 'transaction_batch' ('dbId' INTEGER NOT NULL, 'projectId' INTEGER NOT NULL, 'value' REAL NOT NULL, 'currency' TEXT NOT NULL, PRIMARY KEY('dbId'))")
                database.execSQL("CREATE TABLE 'transaction_purchase' ('dbId' INTEGER NOT NULL, 'value' REAL NOT NULL, 'currency' TEXT NOT NULL, 'beneficiaryId' INTEGER NOT NULL, 'createdAt' TEXT NOT NULL, 'transactionId' INTEGER NOT NULL, PRIMARY KEY('dbId'))")
            }
        }
    }
}
