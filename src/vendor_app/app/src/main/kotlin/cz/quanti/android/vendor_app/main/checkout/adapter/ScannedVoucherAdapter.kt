package cz.quanti.android.vendor_app.main.checkout.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.viewholder.ScannedVouchersViewHolder
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher

class ScannedVoucherAdapter : RecyclerView.Adapter<ScannedVouchersViewHolder>() {

    private val vouchers: MutableList<Voucher> = mutableListOf()

    fun setData(data: List<Voucher>) {
        vouchers.clear()
        vouchers.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedVouchersViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return ScannedVouchersViewHolder(view)
    }

    override fun getItemCount(): Int {
        return vouchers.size
    }

    override fun onBindViewHolder(holder: ScannedVouchersViewHolder, position: Int) {
        val item = vouchers[position]
        holder.text.text = "${item.value} ${item.currency}"
    }
}
