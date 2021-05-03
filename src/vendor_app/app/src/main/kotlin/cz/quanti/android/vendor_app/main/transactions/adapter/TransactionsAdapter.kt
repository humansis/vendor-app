package cz.quanti.android.vendor_app.main.transactions.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.transactions.viewholder.TransactionsViewHolder
import cz.quanti.android.vendor_app.repository.purchase.dto.Transaction

class TransactionsAdapter(
    private val context: Context
    ) : RecyclerView.Adapter<TransactionsViewHolder>() {

    private val transactions: MutableList<Transaction> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transactions, parent, false)
        return TransactionsViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        val item = transactions[position]

        holder.projectId.text = context.getString(R.string.project_number, item.projectId)
        //todo naplnit tabulku
        holder.quantity.text = context.getString(R.string.quantity, item.purchaseIds.size)
        holder.total.text = context.getString(R.string.total_price, item.value, item.currency)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    fun setData(data: List<Transaction>) {
        transactions.clear()
        transactions.addAll(data)
        notifyDataSetChanged()
    }

}
