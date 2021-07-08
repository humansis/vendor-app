package cz.quanti.android.vendor_app.repository

import androidx.room.Database
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
    ], version = 5, exportSchema = false
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

        val MIGRATION_3_4 = object : Migration(3,4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE 'card_purchase_new' ('dbId' INTEGER NOT NULL, 'card' TEXT, 'purchaseId' INTEGER NOT NULL, PRIMARY KEY('dbId'), FOREIGN KEY('purchaseId') REFERENCES 'purchase'('dbId') ON DELETE CASCADE)")
                database.execSQL("INSERT INTO card_purchase_new (dbId, card, purchaseId) SELECT dbId, card, purchaseId FROM card_purchase")
                database.execSQL("DROP TABLE card_purchase")
                database.execSQL("ALTER TABLE card_purchase_new RENAME TO card_purchase")

                database.execSQL("CREATE TABLE 'voucher_purchase_new' ('dbId' INTEGER NOT NULL, 'voucher' INTEGER NOT NULL, 'purchaseId' INTEGER NOT NULL, PRIMARY KEY('dbId'), FOREIGN KEY('purchaseId') REFERENCES 'purchase'('dbId') ON DELETE CASCADE)")
                database.execSQL("INSERT INTO voucher_purchase_new (dbId, voucher, purchaseId) SELECT dbId, voucher, purchaseId FROM voucher_purchase")
                database.execSQL("DROP TABLE voucher_purchase")
                database.execSQL("ALTER TABLE voucher_purchase_new RENAME TO voucher_purchase")
            }
        }

        val MIGRATION_4_5 = object : Migration(4,5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX index_card_purchase_purchaseId ON card_purchase(purchaseId)")
                database.execSQL("CREATE INDEX index_voucher_purchase_purchaseId ON voucher_purchase(purchaseId)")
            }
        }
    }
}
