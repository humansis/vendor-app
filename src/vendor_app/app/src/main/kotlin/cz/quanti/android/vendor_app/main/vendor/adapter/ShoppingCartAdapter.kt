package cz.quanti.android.vendor_app.main.vendor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.viewholder.ShoppingCartViewHolder
import cz.quanti.android.vendor_app.repository.entity.SelectedProduct
import java.text.DecimalFormat
import kotlin.math.roundToInt

class ShoppingCartAdapter : RecyclerView.Adapter<ShoppingCartViewHolder>() {

    private val cart: MutableList<SelectedProduct> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingCartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping_cart, parent, false)
        return ShoppingCartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cart.size
    }

    override fun onBindViewHolder(holder: ShoppingCartViewHolder, position: Int) {
        val item = cart[position]

        // TODO handle images
        holder.productDetail.text = item.product.name + " " + getStringFromDouble(item.quantity) + " " + item.product.unit
        holder.price.text = getStringFromDouble(item.subTotal)
    }

    fun add(product: SelectedProduct) {
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
        notifyDataSetChanged()
    }

    private fun getStringFromDouble(double: Double): String {
        return when {
            double % 1.0 < 0.001 -> {
                double.roundToInt().toString()
            }
            (double * 10) % 1.0 < 0.01 -> {
                DecimalFormat("#.#").format(double)
            }
            else -> {
                DecimalFormat("#.##").format(double)
            }
        }
    }
}
