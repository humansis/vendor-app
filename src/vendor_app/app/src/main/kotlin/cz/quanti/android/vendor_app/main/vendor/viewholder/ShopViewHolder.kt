package cz.quanti.android.vendor_app.main.vendor.viewholder

import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_product.view.*

class ShopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var firstProductName: TextView? = itemView.productName
    var firstProductImage: ImageView? = itemView.productImage
    var firstProductLayout: ConstraintLayout? = itemView.productLayout
}
