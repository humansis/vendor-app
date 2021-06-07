package cz.quanti.android.vendor_app.main.vendor.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_shopping_cart.view.*
import kotlinx.android.synthetic.main.item_shopping_cart.view.priceTextView
import kotlinx.android.synthetic.main.item_shopping_cart.view.productImageView
import kotlinx.android.synthetic.main.item_shopping_cart.view.productInfoTextView
import kotlinx.android.synthetic.main.product_options.view.*

class ShoppingCartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var image: ImageView = itemView.productImageView
    var productDetail: TextView = itemView.productInfoTextView
    var price: TextView = itemView.priceTextView
    var remove: ImageView = itemView.deleteButton
}
