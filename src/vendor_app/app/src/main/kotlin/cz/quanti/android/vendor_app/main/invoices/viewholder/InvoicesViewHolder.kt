package cz.quanti.android.vendor_app.main.invoices.viewholder

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.databinding.ItemInvoiceBinding

class InvoicesViewHolder(invoiceBinding: ItemInvoiceBinding) :
    RecyclerView.ViewHolder(invoiceBinding.root) {
    var invoiceId: TextView = invoiceBinding.invoiceNumberText
    var date: TextView = invoiceBinding.invoiceDateText
    var quantity: TextView = invoiceBinding.invoiceQuantityText
    var total: TextView = invoiceBinding.invoiceTotalText
}
