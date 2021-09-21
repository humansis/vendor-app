package cz.quanti.android.vendor_app.main.shop.viewholder

import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.databinding.ItemCategoryBinding

class CategoryViewHolder(categoryBinding: ItemCategoryBinding) : RecyclerView.ViewHolder(categoryBinding.root) {
    var categoryName: TextView = categoryBinding.categoryName
    var categoryImage: ImageView = categoryBinding.categoryImage
    var categoryLayout: ConstraintLayout = categoryBinding.categoryLayout
    var categoryForeground: View = categoryBinding.categoryForeground
}
