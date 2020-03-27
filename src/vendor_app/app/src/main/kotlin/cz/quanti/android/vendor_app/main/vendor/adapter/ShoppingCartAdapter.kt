package cz.quanti.android.vendor_app.main.vendor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.fragment.ShoppingCartFragment
import cz.quanti.android.vendor_app.main.vendor.viewholder.ShoppingCartViewHolder
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.getStringFromDouble

class ShoppingCartAdapter(private val shoppingCartFragment: ShoppingCartFragment) :
    RecyclerView.Adapter<ShoppingCartViewHolder>() {

    private val cart: MutableList<SelectedProduct> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingCartViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_shopping_cart, parent, false)
        return ShoppingCartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cart.size
    }

    override fun onBindViewHolder(holder: ShoppingCartViewHolder, position: Int) {
        val item = cart[position]

        // TODO handle images
        val productDetailText =
            "${item.product.name} ${getStringFromDouble(item.quantity)} ${item.product.unit}"
        val priceText =
            "${getStringFromDouble(item.subTotal)} ${shoppingCartFragment.chosenCurrency}"
        holder.productDetail.text = productDetailText
        holder.price.text = priceText
        holder.remove.setOnClickListener {
            shoppingCartFragment.removeItemFromCart(position)
            cart.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun clearAll() {
        cart.clear()
        notifyDataSetChanged()
    }

    fun setData(data: List<SelectedProduct>) {
        cart.clear()
        for (item in data) {
            add(item)
        }
        notifyDataSetChanged()
    }

    private fun add(product: SelectedProduct) {
        var alreadyInCart = false
        for (item in cart) {
            if (item == product) {
                item.add(product)
                alreadyInCart = true
                break
            }
        }

        if (!alreadyInCart) {
            cart.add(product)
        }
    }
}
