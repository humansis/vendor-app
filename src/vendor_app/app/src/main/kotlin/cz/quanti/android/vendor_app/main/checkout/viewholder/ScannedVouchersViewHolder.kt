package cz.quanti.android.vendor_app.main.checkout.viewholder

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.databinding.ItemVoucherBinding

class ScannedVouchersViewHolder(binding: ItemVoucherBinding) : RecyclerView.ViewHolder(binding.root) {
    var image: ImageView = binding.voucherImage
    var text: TextView = binding.voucherTextView
}
