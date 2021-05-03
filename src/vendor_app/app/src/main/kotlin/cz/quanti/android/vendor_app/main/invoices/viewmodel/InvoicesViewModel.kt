package cz.quanti.android.vendor_app.main.invoices.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.Invoice
import io.reactivex.Single

class InvoicesViewModel(
    private val purchaseFacade: PurchaseFacade
) : ViewModel() {

    fun getInvoices() : Single<List<Invoice>> {
        return purchaseFacade.getInvoices()
    }
}
