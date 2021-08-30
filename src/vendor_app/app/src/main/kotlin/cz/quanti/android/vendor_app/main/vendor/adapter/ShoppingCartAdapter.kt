package cz.quanti.android.vendor_app.main.vendor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.callback.ShoppingCartFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.viewholder.ShoppingCartViewHolder
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.getStringFromDouble
import quanti.com.kotlinlog.Log

class ShoppingCartAdapter(private val shoppingCartFragmentCallback: ShoppingCartFragmentCallback) :
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

        Picasso.get().load(item.product.image)
            .into(holder.image)

        val productDetailText =
            "${item.product.name}"
        val priceText =
            "${getStringFromDouble(item.price)} ${shoppingCartFragmentCallback.getCurrency()}"
        holder.productDetail.text = productDetailText
        holder.price.text = priceText
        holder.remove.setOnClickListener {
            Log.d(TAG, "Remove button clicked.")
            shoppingCartFragmentCallback.removeItemFromCart(position)
        }
    }

    fun removeAt(position: Int) {
        cart.removeAt(position)
        notifyDataSetChanged()
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
        cart.add(product)
    }

    companion object {
        private val TAG = ShoppingCartAdapter::class.java.simpleName
    }
}
