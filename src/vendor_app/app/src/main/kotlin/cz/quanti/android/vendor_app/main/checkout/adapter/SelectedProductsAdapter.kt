package cz.quanti.android.vendor_app.main.checkout.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.checkout.callback.CheckoutFragmentCallback
import cz.quanti.android.vendor_app.main.checkout.viewholder.SelectedProductsViewHolder
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.getStringFromDouble

class SelectedProductsAdapter(private val checkoutFragmentCallback: CheckoutFragmentCallback) :
    RecyclerView.Adapter<SelectedProductsViewHolder>() {

    private val products: MutableList<SelectedProduct> = mutableListOf()
    var chosenCurrency: String = ""
    var expandedCardHolder: SelectedProductsViewHolder? = null

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

        Picasso.get().load(item.product.image)
            .into(holder.image)

        holder.productName.text = item.product.name
        val price = "${getStringFromDouble(item.price)} $chosenCurrency"
        holder.price.text = price

        holder.itemView.setOnClickListener {
            toggleCardViewExpanded(holder, item)
        }

        holder.close.setOnClickListener {
            closeCard(holder)
        }

        holder.remove.setOnClickListener {
            checkoutFragmentCallback.removeItemFromCart(position)
        }

        holder.confirm.setOnClickListener {
            // TODO update cart, vzit udaje z holderu
        }
    }

    fun toggleCardViewExpanded(holder: SelectedProductsViewHolder, item: SelectedProduct) {
        if (expandedCardHolder == holder) {
            closeCard(holder)
            // TODO testnout jestli tohle nedát pryč a nezavírat jen křížkem? třeba se to bude chovat lépe
        } else {
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
        holder.currency.text = chosenCurrency
        holder.confirm.text = "Confirm"
    }

    fun setData(data: List<SelectedProduct>) {
        products.clear()
        products.addAll(data)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        products.removeAt(position)
        notifyDataSetChanged()
    }

    fun clearAll() {
        products.clear()
        notifyDataSetChanged()
    }
}
