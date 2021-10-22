package cz.quanti.android.vendor_app.main.checkout.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.ItemShoppingCartBinding
import cz.quanti.android.vendor_app.main.checkout.callback.CheckoutFragmentCallback
import cz.quanti.android.vendor_app.main.checkout.viewholder.SelectedProductsViewHolder
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import cz.quanti.android.vendor_app.utils.inputFilterDecimal
import cz.quanti.android.vendor_app.utils.round
import quanti.com.kotlinlog.Log
import java.math.BigDecimal

class SelectedProductsAdapter(
    private val checkoutFragmentCallback: CheckoutFragmentCallback,
    private val context: Context
) :
    RecyclerView.Adapter<SelectedProductsViewHolder>() {

    private val products: MutableList<SelectedProduct> = mutableListOf()
    var chosenCurrency: String = ""
    private var expandedCardHolder: SelectedProductsViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedProductsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val selectedProductBinding = ItemShoppingCartBinding.inflate(inflater, parent, false)
        return SelectedProductsViewHolder(selectedProductBinding)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: SelectedProductsViewHolder, position: Int) {
        val item = products[position]

        Glide
            .with(context)
            .load(item.product.image)
            .into(holder.image)

        holder.productName.text = item.product.name
        val price = "${getStringFromDouble(item.price)} $chosenCurrency"
        holder.price.text = price

        holder.itemView.setOnClickListener {
            Log.d(TAG, "Product $item clicked")
            expandCard(holder, item)
        }

        holder.close.setOnClickListener {
            Log.d(TAG, "Close button clicked")
            closeCard(holder)
        }

        holder.remove.setOnClickListener {
            Log.d(TAG, "Remove button clicked")
            checkoutFragmentCallback.removeItemFromCart(item)
        }

        holder.confirm.setOnClickListener {
            Log.d(TAG, "Update button clicked")
            updateProduct(item, holder)
        }
    }

    private fun updateProduct(
        item: SelectedProduct,
        holder: SelectedProductsViewHolder
    ) {
        try {
            val newPrice = holder.priceEditText.text.toString().toDouble()
            if (newPrice <= 0.0) {
                checkoutFragmentCallback.showInvalidPriceEnteredMessage()
            } else {
                item.price = round(newPrice, 3)
                checkoutFragmentCallback.updateItem(item)
                closeCard(holder)
            }
        } catch (e: NumberFormatException) {
            checkoutFragmentCallback.showInvalidPriceEnteredMessage()
        }
    }

    private fun expandCard(holder: SelectedProductsViewHolder, item: SelectedProduct) {
        if (expandedCardHolder != holder) {
            closeExpandedCard()
            expandedCardHolder = holder
            holder.price.visibility = View.GONE
            holder.remove.visibility = View.VISIBLE
            holder.close.visibility = View.VISIBLE
            if (item.product.category.type != CategoryType.CASHBACK) {
                holder.editProduct.visibility = View.VISIBLE
                loadOptions(holder, item)
            }
        }
    }

    private fun closeCard(holder: SelectedProductsViewHolder) {
        holder.price.visibility = View.VISIBLE
        holder.remove.visibility = View.GONE
        holder.close.visibility = View.GONE
        holder.editProduct.visibility = View.GONE
        expandedCardHolder = null
    }

    private fun loadOptions(holder: SelectedProductsViewHolder, item: SelectedProduct) {
        val price = BigDecimal.valueOf(item.price).stripTrailingZeros().toPlainString()
        holder.priceEditText.setText(price)
        holder.priceEditText.inputFilterDecimal(maxDigitsIncludingPoint = 10, maxDecimalPlaces = 2)
        holder.priceTextInputLayout.suffixText = chosenCurrency
        holder.confirm.text = context.getString(R.string.confirm)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<SelectedProduct>) {
        products.clear()
        products.addAll(data)
        notifyDataSetChanged()
    }

    fun closeExpandedCard() {
        expandedCardHolder?.let { closeCard(it) }
    }

    companion object {
        private val TAG = SelectedProductsAdapter::class.java.simpleName
    }
}
