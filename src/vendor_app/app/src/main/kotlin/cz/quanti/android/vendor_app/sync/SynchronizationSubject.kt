package cz.quanti.android.vendor_app.sync

enum class SynchronizationSubject(val message: String) {
    // TODO mit message jako string resource s preklady
    PURCHASES_UPLOAD("Uploading purchases"),
    BOOKLETS_UPLOAD("Uploading deactivated booklets"),
    BOOKLETS_DEACTIVATED_DOWNLOAD("Downloading deactivated booklets"),
    BOOKLETS_PROTECTED_DOWNLOAD("Downloading protected booklets"),
    BLOCKED_CARDS_DOWNLOAD("Downloading blocked smartcards"),
    RD_UPLOAD("Uploading RD"),
    RD_DOWNLOAD("Downloading RD"),
    CATEGORIES_DOWNLOAD("Downloading product categories"),
    PRODUCTS_DOWNLOAD("Downloading products"),
    TRANSACTIONS_DOWNLOAD("Downloading transactions to reimburse"),
    INVOICES_DOWNLOAD("Downloading invoices")
}
