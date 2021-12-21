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
import cz.quanti.android.vendor_app.repository.category.dao.CategoryDao
import cz.quanti.android.vendor_app.repository.category.dto.db.CategoryDbEntity
import cz.quanti.android.vendor_app.repository.deposit.dao.ReliefPackageDao
import cz.quanti.android.vendor_app.repository.deposit.dto.db.ReliefPackageDbEntity
import cz.quanti.android.vendor_app.repository.invoice.dao.InvoiceDao
import cz.quanti.android.vendor_app.repository.invoice.dto.db.InvoiceDbEntity
import cz.quanti.android.vendor_app.repository.product.dao.ProductDao
import cz.quanti.android.vendor_app.repository.product.dto.db.ProductDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dao.CardPurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dao.PurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dao.PurchasedProductDao
import cz.quanti.android.vendor_app.repository.purchase.dao.SelectedProductDao
import cz.quanti.android.vendor_app.repository.purchase.dao.VoucherPurchaseDao
import cz.quanti.android.vendor_app.repository.purchase.dto.db.CardPurchaseDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.PurchaseDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.PurchasedProductDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.SelectedProductDbEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.db.VoucherPurchaseDbEntity
import cz.quanti.android.vendor_app.repository.transaction.dao.TransactionDao
import cz.quanti.android.vendor_app.repository.transaction.dao.TransactionPurchaseDao
import cz.quanti.android.vendor_app.repository.transaction.dto.db.TransactionDbEntity
import cz.quanti.android.vendor_app.repository.transaction.dto.db.TransactionPurchaseDbEntity
import cz.quanti.android.vendor_app.repository.utils.typeconverter.DateTypeConverter

@Database(
    entities = [
        BookletDbEntity::class,
        CategoryDbEntity::class,
        ProductDbEntity::class,
        SelectedProductDbEntity::class,
        PurchasedProductDbEntity::class,
        PurchaseDbEntity::class,
        VoucherPurchaseDbEntity::class,
        CardPurchaseDbEntity::class,
        BlockedCardDbEntity::class,
        InvoiceDbEntity::class,
        TransactionDbEntity::class,
        TransactionPurchaseDbEntity::class,
        ReliefPackageDbEntity::class
    ], version = 10, exportSchema = false
)
@TypeConverters(DateTypeConverter::class)
abstract class VendorDb : RoomDatabase() {
    abstract fun bookletDao(): BookletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun selectedProductDao(): SelectedProductDao
    abstract fun purchasedProductDao(): PurchasedProductDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun voucherPurchaseDao(): VoucherPurchaseDao
    abstract fun cardPurchaseDao(): CardPurchaseDao
    abstract fun blockedCardDao(): BlockedCardDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionPurchaseDao(): TransactionPurchaseDao
    abstract fun reliefPackageDao(): ReliefPackageDao

    companion object {
        const val DB_NAME = "cz.quanti.android.pin.vendor_app.database"
        const val TABLE_BOOKLET = "booklet"
        const val TABLE_CATEGORY = "category"
        const val TABLE_PRODUCT = "product"
        const val TABLE_SELECTED_PRODUCT = "selected_product"
        const val TABLE_PURCHASED_PRODUCT = "purchased_product"
        const val TABLE_PURCHASE = "purchase"
        const val TABLE_VOUCHER_PURCHASE = "voucher_purchase"
        const val TABLE_CARD_PURCHASE = "card_purchase"
        const val TABLE_BLOCKED_SMARTCARD = "blocked_smartcard"
        const val TABLE_INVOICE = "invoice"
        const val TABLE_TRANSACTION_BATCH = "transaction_batch"
        const val TABLE_TRANSACTION_PURCHASE = "transaction_purchase"
        const val TABLE_RELIEF_PACKAGE = "relief_package"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE 'invoice' ('id' INTEGER NOT NULL, 'date' TEXT NOT NULL, 'quantity' INTEGER NOT NULL, 'value' REAL NOT NULL, 'currency' TEXT NOT NULL, PRIMARY KEY('id'))")
                database.execSQL("CREATE TABLE 'transaction_batch' ('dbId' INTEGER NOT NULL, 'projectId' INTEGER NOT NULL, 'value' REAL NOT NULL, 'currency' TEXT NOT NULL, PRIMARY KEY('dbId'))")
                database.execSQL("CREATE TABLE 'transaction_purchase' ('dbId' INTEGER NOT NULL, 'value' REAL NOT NULL, 'currency' TEXT NOT NULL, 'beneficiaryId' INTEGER NOT NULL, 'createdAt' TEXT NOT NULL, 'transactionId' INTEGER NOT NULL, PRIMARY KEY('dbId'))")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX index_card_purchase_purchaseId ON card_purchase(purchaseId)")
                database.execSQL("CREATE INDEX index_voucher_purchase_purchaseId ON voucher_purchase(purchaseId)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE 'purchased_product' ('dbId' INTEGER NOT NULL, 'productId' INTEGER NOT NULL, 'value' REAL NOT NULL, 'purchaseId' INTEGER NOT NULL, PRIMARY KEY('dbId'), FOREIGN KEY('purchaseId') REFERENCES 'purchase'('dbId') ON DELETE CASCADE)")
                database.execSQL("INSERT INTO purchased_product (dbId, productId, value, purchaseId) SELECT dbId, productId, value, purchaseId FROM selected_product")
                database.execSQL("CREATE INDEX index_purchased_product_purchaseId ON purchased_product(purchaseId)")
                database.execSQL("DROP TABLE selected_product")
                database.execSQL("CREATE TABLE 'selected_product' ('dbId' INTEGER NOT NULL, 'productId' INTEGER NOT NULL, 'value' REAL NOT NULL, PRIMARY KEY('dbId'))")
                database.execSQL("ALTER TABLE 'purchase' ADD 'currency' TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'card_purchase' ADD 'beneficiaryId' INTEGER")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DELETE FROM product")
                database.execSQL("ALTER TABLE product ADD categoryId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE product ADD unitPrice REAL")
                database.execSQL("ALTER TABLE product ADD currency TEXT")
                database.execSQL("CREATE TABLE 'category' ('id' INTEGER NOT NULL, 'name' TEXT NOT NULL, 'type' TEXT NOT NULL, 'image' TEXT, PRIMARY KEY('id'))")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE relief_package (id INTEGER NOT NULL, assistanceId INTEGER NOT NULL, beneficiaryId INTEGER NOT NULL, amount REAL NOT NULL, currency TEXT NOT NULL, tagId TEXT NOT NULL, foodLimit REAL, nonfoodLimit REAL, cashbackLimit REAL, expirationDate INTEGER, createdAt TEXT, balanceBefore REAL, balanceAfter REAL, PRIMARY KEY('id'))")
                database.execSQL("ALTER TABLE card_purchase ADD assistanceId INTEGER")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE card_purchase ADD balanceBefore REAL")
                database.execSQL("ALTER TABLE card_purchase ADD balanceAfter REAL")
            }
        }
    }
}
