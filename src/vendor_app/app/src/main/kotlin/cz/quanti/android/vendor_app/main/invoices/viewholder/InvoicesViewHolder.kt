package cz.quanti.android.vendor_app.main.invoices.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_invoice.view.*

class InvoicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var invoiceId: TextView = itemView.invoice_number_text
    var date: TextView = itemView.invoice_date_text
    var quantity: TextView  = itemView.invoice_quantity_text
    var total: TextView = itemView.invoice_total_text
}
