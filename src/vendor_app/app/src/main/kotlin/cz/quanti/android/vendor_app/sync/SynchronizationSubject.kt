package cz.quanti.android.vendor_app.sync

import cz.quanti.android.vendor_app.R

enum class SynchronizationSubject(val message: Int) {
    PURCHASES_UPLOAD(R.string.purchases_upload),
    RD_UPLOAD(R.string.rd_upload),
    RD_DOWNLOAD(R.string.rd_download),
    BLOCKED_CARDS_DOWNLOAD(R.string.blocked_cards_download),
    CATEGORIES_DOWNLOAD(R.string.categories_download),
    PRODUCTS_DOWNLOAD(R.string.products_download),
    TRANSACTIONS_DOWNLOAD(R.string.transactions_download),
    INVOICES_DOWNLOAD(R.string.invoices_download),
    LOGS_UPLOAD(R.string.logs_upload)
}
