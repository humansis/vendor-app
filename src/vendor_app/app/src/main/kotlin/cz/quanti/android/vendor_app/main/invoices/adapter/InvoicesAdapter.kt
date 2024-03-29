package cz.quanti.android.vendor_app.main.invoices.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.ItemInvoiceBinding
import cz.quanti.android.vendor_app.main.invoices.viewholder.InvoicesViewHolder
import cz.quanti.android.vendor_app.repository.invoice.dto.Invoice
import cz.quanti.android.vendor_app.utils.convertStringToDateFormattedString

class InvoicesAdapter(
    private val context: Context
) : RecyclerView.Adapter<InvoicesViewHolder>() {

    private val invoices: MutableList<Invoice> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoicesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val invoiceBinding = ItemInvoiceBinding.inflate(inflater, parent, false)
        return InvoicesViewHolder(invoiceBinding)
    }

    override fun onBindViewHolder(holder: InvoicesViewHolder, position: Int) {
        val item = invoices[position]

        holder.invoiceId.text = context.getString(R.string.humansis_invoice_number, item.invoiceId)
        holder.date.text = context.getString(
            R.string.date,
            convertStringToDateFormattedString(context, item.date) ?: R.string.unknown
        )
        holder.quantity.text = context.getString(R.string.quantity, item.quantity)
        holder.total.text = context.getString(R.string.total_price, item.value, item.currency)
    }

    override fun getItemCount(): Int {
        return invoices.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Invoice>) {
        invoices.clear()
        invoices.addAll(data.sortedByDescending { it.date })
        notifyDataSetChanged()
    }
}
