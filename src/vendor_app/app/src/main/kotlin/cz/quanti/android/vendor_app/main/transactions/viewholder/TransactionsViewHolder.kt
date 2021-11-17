package cz.quanti.android.vendor_app.main.transactions.viewholder

import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.databinding.ItemTransactionBinding

class TransactionsViewHolder(transactionBinding: ItemTransactionBinding) :
    RecyclerView.ViewHolder(transactionBinding.root) {
    var projectId: TextView = transactionBinding.transactionsProjectId
    var purchasesTable: TableLayout = transactionBinding.purchasesTable
    var quantity: TextView = transactionBinding.transactionsQuantity
    var total: TextView = transactionBinding.transactionsTotal
    var tableToggle: ImageView = transactionBinding.tableToggle
    var cardView: CardView = transactionBinding.transactionsCardview
}
