package cz.quanti.android.vendor_app.main.checkout.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_checkout_selected_product.view.*
import kotlinx.android.synthetic.main.item_shopping_cart.view.priceTextView
import kotlinx.android.synthetic.main.item_shopping_cart.view.productImageView
import kotlinx.android.synthetic.main.item_shopping_cart.view.productInfoTextView

class SelectedProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var image: ImageView = itemView.productImageView
    var productDetail: TextView = itemView.productInfoTextView
    var amount: TextView = itemView.amountTextView
    var price: TextView = itemView.priceTextView
}
