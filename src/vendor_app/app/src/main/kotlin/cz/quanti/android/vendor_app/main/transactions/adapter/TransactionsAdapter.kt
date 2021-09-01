package cz.quanti.android.vendor_app.main.transactions.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.ItemTransactionBinding
import cz.quanti.android.vendor_app.main.transactions.viewholder.TransactionsViewHolder
import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.utils.convertStringToDate
import quanti.com.kotlinlog.Log

class TransactionsAdapter(
    private val context: Context
) : RecyclerView.Adapter<TransactionsViewHolder>() {

    private val transactions: MutableList<Transaction> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val transactionBinding = ItemTransactionBinding.inflate(inflater, parent, false)
        return TransactionsViewHolder(transactionBinding)
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        val item = transactions[position]

        holder.projectId.text = (context.getString(R.string.project) + "  ${item.projectId}") // todo replace later with project name
        holder.quantity.text = context.getString(R.string.quantity, item.purchases.size)
        holder.total.text = context.getString(R.string.total_price, item.value, item.currency)

        prepareTable(item, holder)
    }

    private fun prepareTable(item: Transaction, holder: TransactionsViewHolder) {
        holder.purchasesTable.visibility = View.GONE
        holder.tableToggle.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
        holder.cardView.setOnClickListener {
            if (holder.purchasesTable.visibility == View.GONE) {
                Log.d(TAG, "Transactions table opened.")
                if (holder.purchasesTable.isEmpty()) {
                    loadTable(item, holder)
                }
                holder.purchasesTable.visibility = View.VISIBLE
                holder.tableToggle.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            } else {
                Log.d(TAG, "Transactions table closed.")
                holder.purchasesTable.visibility = View.GONE
                holder.tableToggle.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
        }
    }

    private fun loadTable(item: Transaction, holder: TransactionsViewHolder) {
        val inflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

        if (item.purchases.isEmpty()) {
            val tv = TextView(context, null)
            tv.text = context.getString(R.string.no_purchases)
            holder.purchasesTable.addView(tv)
        } else {
            item.purchases.forEach { tp ->
                val row = inflater.inflate(R.layout.item_transaction_purchase, TableRow(context))
                row.findViewById<TextView>(R.id.transaction_purchase_date).text = context.getString(
                    R.string.date,
                    convertStringToDate(context, tp.createdAt) ?: R.string.unknown
                )
                row.findViewById<TextView>(R.id.transaction_purchase_total).text = context.getString(
                    R.string.total_price,
                    tp.value, tp.currency
                )
                holder.purchasesTable.addView(row)
            }
        }
        val line = inflater.inflate(R.layout.item_line, TableRow(context))
        holder.purchasesTable.addView(line)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Transaction>) {
        transactions.clear()
        transactions.addAll(data)
        notifyDataSetChanged()
    }

    companion object {
        private val TAG = TransactionsAdapter::class.java.simpleName
    }

}
