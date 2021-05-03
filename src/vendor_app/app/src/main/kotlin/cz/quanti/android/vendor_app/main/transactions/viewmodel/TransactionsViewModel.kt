package cz.quanti.android.vendor_app.main.transactions.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.Invoice
import cz.quanti.android.vendor_app.repository.purchase.dto.Transaction
import cz.quanti.android.vendor_app.repository.purchase.dto.TransactionPurchase
import io.reactivex.Single

class TransactionsViewModel(
    private val purchaseFacade: PurchaseFacade
) : ViewModel() {

    fun getTransactions() : Single<List<Transaction>> {
        return purchaseFacade.getTransactions()
    }

    fun getTransactionPurchases(purchaseIds: List<Long>) : Single<List<TransactionPurchase>> {
        return purchaseFacade.getTransactionPurchases(purchaseIds)
    }
}
