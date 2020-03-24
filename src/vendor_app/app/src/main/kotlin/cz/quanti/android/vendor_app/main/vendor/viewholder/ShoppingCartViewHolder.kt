package cz.quanti.android.vendor_app.main.vendor.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_shopping_cart.view.*

class ShoppingCartViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var image: ImageView = itemView.productImageView
    var productDetail: TextView = itemView.productInfoTextView
    var price: TextView = itemView.priceTextView
}
