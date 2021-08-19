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
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import cz.quanti.android.vendor_app.utils.round
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
            expandCard(holder, item)
        }

        holder.close.setOnClickListener {
            closeCard(holder)
        }

        holder.remove.setOnClickListener {
            checkoutFragmentCallback.removeItemFromCart(item)
        }

        holder.confirm.setOnClickListener {
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
                checkoutFragmentCallback.updateItem(
                    item,
                    round(newPrice, 3)
                )
                closeCard(holder)
            }
        } catch(e: NumberFormatException) {
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
            holder.editProduct.visibility = View.VISIBLE
            loadOptions(holder, item)
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
}
