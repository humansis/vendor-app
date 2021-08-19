package cz.quanti.android.vendor_app.main.checkout.viewholder

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.databinding.ItemVoucherBinding

class ScannedVouchersViewHolder(voucherBinding: ItemVoucherBinding) : RecyclerView.ViewHolder(voucherBinding.root) {
    var image: ImageView = voucherBinding.voucherImage
    var text: TextView = voucherBinding.voucherTextView
}
