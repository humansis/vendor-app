package cz.quanti.android.vendor_app.main.vendor.viewholder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.fragment.ProductDetailFragment
import cz.quanti.android.vendor_app.main.vendor.fragment.ShoppingCartFragment
import kotlinx.android.synthetic.main.item_shop.view.*

class ShopViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var firstProductName: TextView? = itemView.firstProductTitle
    var firstProductImage: LinearLayout? = itemView.firstProductImage
    var secondProductName: TextView? = itemView.secondProductTitle
    var secondProductImage: LinearLayout? = itemView.secondProductImage
    var thirdProductName: TextView? = itemView.thirdProductTitle
    var thirdProductImage: LinearLayout? = itemView.thirdProductImage
    var layout: ConstraintLayout? = itemView.itemShop

    override fun onClick(v: View?) {
        val productDetailFragment = ProductDetailFragment()
        val transaction = (v?.context as FragmentActivity).supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, productDetailFragment)
        }
        transaction?.commit()
    }
}
