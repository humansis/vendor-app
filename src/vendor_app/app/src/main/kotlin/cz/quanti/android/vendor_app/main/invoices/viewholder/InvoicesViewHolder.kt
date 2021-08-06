package cz.quanti.android.vendor_app.main.invoices.viewholder

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.databinding.ItemInvoiceBinding

class InvoicesViewHolder(binding: ItemInvoiceBinding) : RecyclerView.ViewHolder(binding.root) {
    var invoiceId: TextView = binding.invoiceNumberText
    var date: TextView = binding.invoiceDateText
    var quantity: TextView  = binding.invoiceQuantityText
    var total: TextView = binding.invoiceTotalText
}
