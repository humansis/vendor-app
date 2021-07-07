package cz.quanti.android.vendor_app.main.checkout.viewholder

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.item_product_options.view.*
import kotlinx.android.synthetic.main.item_shopping_cart.view.*

class SelectedProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var image: ImageView = itemView.productImageView
    var productName: TextView = itemView.productInfoTextView
    var price: TextView = itemView.priceTextView
    var close: ImageView = itemView.closeButton
    var editProduct: FrameLayout = itemView.editProduct
    var priceEditText: TextInputEditText = itemView.priceEditText
    var priceTextInputLayout: TextInputLayout = itemView.priceTextInputLayout
    var remove: ImageView = itemView.deleteButton
    var confirm: MaterialButton = itemView.confirmButton
}
