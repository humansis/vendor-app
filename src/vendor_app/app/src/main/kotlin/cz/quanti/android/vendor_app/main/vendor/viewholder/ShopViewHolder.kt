package cz.quanti.android.vendor_app.main.vendor.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_product.view.*
import kotlinx.android.synthetic.main.item_shop.view.*

class ShopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var firstProductName: TextView? = itemView.firstProduct.productName
    var firstProductImage: ImageView? = itemView.firstProduct.productImage
    var firstProductLayout: CardView? = itemView.firstProduct.productLayout
    var secondProductName: TextView? = itemView.secondProduct.productName
    var secondProductImage: ImageView? = itemView.secondProduct.productImage
    var secondProductLayout: CardView? = itemView.secondProduct.productLayout
    var thirdProductName: TextView? = itemView.thirdProduct.productName
    var thirdProductImage: ImageView? = itemView.thirdProduct.productImage
    var thirdProductLayout: CardView? = itemView.thirdProduct.productLayout
}
