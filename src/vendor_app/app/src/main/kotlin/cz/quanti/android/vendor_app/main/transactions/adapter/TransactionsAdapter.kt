package cz.quanti.android.vendor_app.main.transactions.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.transactions.viewholder.TransactionsViewHolder
import cz.quanti.android.vendor_app.repository.purchase.dto.Transaction
import cz.quanti.android.vendor_app.utils.convertStringToDate
import kotlinx.android.synthetic.main.nav_header.view.*

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
        holder.quantity.text = context.getString(R.string.quantity, item.purchases.size)
        holder.total.text = context.getString(R.string.total_price, item.value, item.currency)

        prepareTable(item, holder)
    }

    private fun prepareTable(item: Transaction, holder: TransactionsViewHolder) {
        val inflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

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
        val line = inflater.inflate(R.layout.view_line, TableRow(context))
        holder.purchasesTable.addView(line)

        tableVisibilityToggle(holder)
    }

    private fun tableVisibilityToggle(holder: TransactionsViewHolder) {
        holder.purchasesTable.visibility = View.GONE
        holder.tableToggle.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
        holder.cardView.setOnClickListener {
            if (holder.purchasesTable.visibility == View.GONE) {
                holder.purchasesTable.visibility = View.VISIBLE
                holder.tableToggle.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            } else {
                holder.purchasesTable.visibility = View.GONE
                holder.tableToggle.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
        }
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
