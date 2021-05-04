package cz.quanti.android.vendor_app.main.transactions.callback

interface TransactionsFragmentCallback {
    fun reloadTransactionsFromDb()
    fun setUpWarning()
    fun disableWarningButton()
}
