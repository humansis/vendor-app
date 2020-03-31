package cz.quanti.android.vendor_app.main.checkout.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_voucher.view.*

class ScannedVouchersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var image: ImageView = itemView.voucherImage
    var text: TextView = itemView.voucherTextView
}
