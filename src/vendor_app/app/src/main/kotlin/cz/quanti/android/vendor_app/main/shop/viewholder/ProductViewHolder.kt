package cz.quanti.android.vendor_app.main.shop.viewholder

import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.databinding.ItemProductBinding

class ProductViewHolder(productBinding: ItemProductBinding) :
    RecyclerView.ViewHolder(productBinding.root) {
    var productName: TextView = productBinding.productName
    var productImage: ImageView = productBinding.productImage
    var productLayout: ConstraintLayout = productBinding.productLayout
}
