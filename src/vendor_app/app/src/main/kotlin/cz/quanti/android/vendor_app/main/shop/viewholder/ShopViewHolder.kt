package cz.quanti.android.vendor_app.main.shop.viewholder

import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.databinding.ItemProductBinding

class ShopViewHolder(binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
    var productName: TextView = binding.productName
    var productImage: ImageView = binding.productImage
    var productLayout: ConstraintLayout = binding.productLayout
}
