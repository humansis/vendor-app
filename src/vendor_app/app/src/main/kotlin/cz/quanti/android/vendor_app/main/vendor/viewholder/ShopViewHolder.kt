package cz.quanti.android.vendor_app.main.vendor.viewholder

import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.item_product.view.*
import kotlinx.android.synthetic.main.item_product_options.view.*
import kotlinx.android.synthetic.main.item_shop.view.*

class ShopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var firstProduct: View? = itemView.firstProduct
    var firstProductName: TextView? = itemView.firstProduct.productName
    var firstProductImage: ImageView? = itemView.firstProduct.productImage
    var firstProductLayout: ConstraintLayout? = itemView.firstProduct.productLayout
    var firstProductPaddingLeft: View? = itemView.firstProduct.productPaddingLeft
    var firstProductPaddingRight: View? = itemView.firstProduct.productPaddingRight
    var firstProductCloseButton: View? = itemView.firstProduct.closeButton
    var firstProductPriceTextInputLayout: TextInputLayout? = itemView.firstProduct.editProduct.priceTextInputLayout
    var firstProductPriceEditText: EditText? = itemView.firstProduct.editProduct.priceEditText
    var firstProductConfirmButton: MaterialButton? = itemView.firstProduct.editProduct.confirmButton

    var firstProductOptions: FrameLayout? = itemView.firstProduct.editProduct
    var secondProduct: View? = itemView.secondProduct
    var secondProductName: TextView? = itemView.secondProduct.productName
    var secondProductImage: ImageView? = itemView.secondProduct.productImage
    var secondProductLayout: ConstraintLayout? = itemView.secondProduct.productLayout
    var thirdProduct: View? = itemView.thirdProduct
    var thirdProductName: TextView? = itemView.thirdProduct.productName
    var thirdProductImage: ImageView? = itemView.thirdProduct.productImage
    var thirdProductLayout: ConstraintLayout? = itemView.thirdProduct.productLayout
}
