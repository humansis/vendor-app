package cz.quanti.android.vendor_app.main.transactions.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_transactions.view.*

class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var projectId: TextView = itemView.transactions_project_id
    var purchasesTable :TableLayout = itemView.purchases_table
    var quantity: TextView  = itemView.transactions_quantity
    var total: TextView = itemView.transactions_total
    var tableToggle: ImageView = itemView.table_toggle
}
