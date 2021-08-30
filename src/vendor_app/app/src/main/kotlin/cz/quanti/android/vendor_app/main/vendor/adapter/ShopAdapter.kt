package cz.quanti.android.vendor_app.main.vendor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.callback.VendorFragmentCallback
import cz.quanti.android.vendor_app.main.vendor.viewholder.ShopViewHolder
import cz.quanti.android.vendor_app.repository.product.dto.Product
import org.koin.core.KoinComponent
import quanti.com.kotlinlog.Log
import kotlin.math.ceil

class ShopAdapter(
    private val vendorFragmentCallback: VendorFragmentCallback,
    private val context: Context
) :
    RecyclerView.Adapter<ShopViewHolder>(), KoinComponent {

    private val products: MutableList<Product> = mutableListOf()
    private val itemsInRow = 3
    private val picasso = Picasso.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    fun setData(data: List<Product>) {
        products.clear()
        products.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return ceil(products.size.toDouble() / itemsInRow).toInt()
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {

        val actualPosition = position * 3
        val productsRow = getProductsRow(holder, actualPosition)

        if (productsRow[0] != null) {
            holder.firstProductName?.text = productsRow[0]?.name
            holder.firstProductImage?.isClickable = true
            picasso.isLoggingEnabled = true
            var img = ImageView(context)
            picasso.load(productsRow[0]?.image)
                .into(img, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        holder.firstProductImage?.background = img.drawable
                    }

                    override fun onError(e: java.lang.Exception?) {
                        Log.e(TAG, e?.message ?: "")
                    }
                })
            holder.firstProductImage?.setOnClickListener {
                Log.d(TAG, "Product clicked.")
                productsRow[0]?.let { product -> selectItem(holder.itemView, product) }
            }
        } else {
            holder.firstProductImage?.visibility = View.INVISIBLE
            holder.firstProductName?.visibility = View.INVISIBLE
            holder.firstProductLayout?.visibility = View.INVISIBLE
        }

        if (productsRow[1] != null) {
            holder.secondProductName?.text = productsRow[1]?.name
            holder.secondProductImage?.isClickable = true
            var img = ImageView(context)
            picasso.load(productsRow[1]?.image)
                .into(img, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        holder.secondProductImage?.background = img.drawable
                    }

                    override fun onError(e: java.lang.Exception?) {
                        Log.e(e?.message ?: "")
                    }
                })
            holder.secondProductImage?.setOnClickListener {
                Log.d(TAG, "Product clicked.")
                productsRow[1]?.let { product -> selectItem(holder.itemView, product) }
            }
        } else {
            holder.secondProductImage?.visibility = View.INVISIBLE
            holder.secondProductName?.visibility = View.INVISIBLE
            holder.secondProductLayout?.visibility = View.INVISIBLE
        }

        if (productsRow[2] != null) {
            holder.thirdProductName?.text = productsRow[2]?.name
            holder.thirdProductImage?.isClickable = true
            var img = ImageView(context)
            picasso.load(productsRow[2]?.image)
                .into(img, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        holder.thirdProductImage?.background = img.drawable
                    }

                    override fun onError(e: java.lang.Exception?) {
                        Log.e(e?.message ?: "")
                    }
                })
            holder.thirdProductImage?.setOnClickListener {
                Log.d(TAG, "Product clicked.")
                productsRow[2]?.let { product -> selectItem(holder.itemView, product) }
            }
        } else {
            holder.thirdProductImage?.visibility = View.INVISIBLE
            holder.thirdProductName?.visibility = View.INVISIBLE
            holder.thirdProductLayout?.visibility = View.INVISIBLE
        }
    }

    private fun getProductsRow(holder: ShopViewHolder, position: Int): Array<Product?> {
        val productsRow = Array<Product?>(3) { null }

        for (i in 0..2) {
            if (products.size > position + i) {
                productsRow[i] = products[position + i]
            }
        }

        return productsRow
    }

    private fun selectItem(itemView: View, product: Product) {
        vendorFragmentCallback.chooseProduct(product)
    }

    companion object {
        private val TAG = ShopAdapter::class.java.simpleName
    }
}
