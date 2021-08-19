package cz.quanti.android.vendor_app.main.checkout.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.databinding.ItemVoucherBinding
import cz.quanti.android.vendor_app.main.checkout.viewholder.ScannedVouchersViewHolder
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher

class ScannedVoucherAdapter : RecyclerView.Adapter<ScannedVouchersViewHolder>() {

    private val vouchers: MutableList<Voucher> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Voucher>) {
        vouchers.clear()
        vouchers.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedVouchersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val voucherBinding = ItemVoucherBinding.inflate(inflater, parent, false)
        return ScannedVouchersViewHolder(voucherBinding)
    }

    override fun getItemCount(): Int {
        return vouchers.size
    }

    override fun onBindViewHolder(holder: ScannedVouchersViewHolder, position: Int) {
        val item = vouchers[position]
        val text = "${item.value} ${item.currency}"
        holder.text.text = text
    }
}
