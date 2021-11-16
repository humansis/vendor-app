package cz.quanti.android.vendor_app.main.checkout.viewholder

import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import cz.quanti.android.vendor_app.databinding.ItemShoppingCartBinding

class SelectedProductsViewHolder(
    selectedProductBinding: ItemShoppingCartBinding
) : RecyclerView.ViewHolder(selectedProductBinding.root) {
    var image: ImageView = selectedProductBinding.productImageView
    var productName: TextView = selectedProductBinding.productInfoTextView
    var price: TextView = selectedProductBinding.priceTextView
    var close: ImageView = selectedProductBinding.closeButton
    var editProduct: FrameLayout = selectedProductBinding.editProduct
    var priceEditText: TextInputEditText = selectedProductBinding.productOptions.priceEditText
    var priceTextInputLayout: TextInputLayout =
        selectedProductBinding.productOptions.priceTextInputLayout
    var remove: ImageView = selectedProductBinding.deleteButton
    var confirm: MaterialButton = selectedProductBinding.productOptions.confirmButton
}
