package cz.quanti.android.vendor_app.main.vendor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.fragment.ProductDetailFragment
import cz.quanti.android.vendor_app.main.vendor.viewholder.ShopViewHolder
import cz.quanti.android.vendor_app.repository.entity.Product
import kotlinx.android.synthetic.main.fragment_product_detail.*
import kotlin.math.ceil

class ShopAdapter: RecyclerView.Adapter<ShopViewHolder>() {

    private val products: MutableList<Product> = mutableListOf()
    private val itemsInRow = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ceil(products.size.toDouble() / itemsInRow).toInt()
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {

        val actualPosition = position * 3
        val productsRow = getProductsRow(holder, actualPosition)

        if(productsRow[0] != null) {
            holder.firstProductName?.text = productsRow[0]!!.name
            //holder.firstProductImage?.background = productsRow[0]!!.image
        } else {
            holder.firstProductImage?.visibility = View.INVISIBLE
            holder.firstProductName?.visibility = View.INVISIBLE
        }

        if(productsRow[1] != null) {
            holder.secondProductName?.text = productsRow[1]!!.name
        } else {
            holder.secondProductImage?.visibility = View.INVISIBLE
            holder.secondProductName?.visibility = View.INVISIBLE
        }

        if(productsRow[2] != null) {
            holder.thirdProductName?.text = productsRow[2]!!.name
        } else {
            holder.thirdProductImage?.visibility = View.INVISIBLE
            holder.thirdProductName?.visibility = View.INVISIBLE
        }
        //TODO
    }

    private fun getProductsRow(holder: ShopViewHolder, position: Int): Array<Product?> {
        val productsRow = Array<Product?>(3) { null }

        for(i in 0..2) {
            if(products.size > position + i)
            {
                productsRow[i] = products[position + i]
            }
        }

        return productsRow
    }
}
