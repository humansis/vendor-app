package cz.quanti.android.vendor_app.main.checkout.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.callback.CheckoutFragmentCallback
import cz.quanti.android.vendor_app.main.checkout.viewholder.SelectedProductsViewHolder
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import cz.quanti.android.vendor_app.utils.round

class SelectedProductsAdapter(
    private val checkoutFragmentCallback: CheckoutFragmentCallback,
    private val context: Context
    ) :
    RecyclerView.Adapter<SelectedProductsViewHolder>() {

    private val products: MutableList<SelectedProduct> = mutableListOf()
    var chosenCurrency: String = ""
    private var expandedCardHolder: SelectedProductsViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedProductsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_cart, parent, false)

        return SelectedProductsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: SelectedProductsViewHolder, position: Int) {
        val item = products[position]

        holder.image.setImageDrawable(item.product.drawable)

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
            checkoutFragmentCallback.removeItemFromCart(position)
        }

        holder.confirm.setOnClickListener {
            updateProduct(position, item, holder)
        }
    }

    private fun updateProduct(
        position: Int,
        item: SelectedProduct,
        holder: SelectedProductsViewHolder
    ) {
        try {
            val newPrice = holder.priceEditText.text.toString().toDouble()
            if (newPrice <= 0.0) {
                checkoutFragmentCallback.showInvalidPriceEnteredMessage()
            } else {
                checkoutFragmentCallback.updateItem(
                    position,
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
            expandedCardHolder?.let { closeCard(it) }
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
        holder.priceEditText.setText(item.price.toString())
        holder.priceTextInputLayout.suffixText = chosenCurrency
        holder.confirm.text = context.getString(R.string.confirm)
    }

    fun setData(data: List<SelectedProduct>) {
        products.clear()
        products.addAll(data)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        expandedCardHolder?.let { closeCard(it) }
        products.removeAt(position)
        notifyDataSetChanged()
    }

    fun clearAll() {
        products.clear()
        notifyDataSetChanged()
    }
}
