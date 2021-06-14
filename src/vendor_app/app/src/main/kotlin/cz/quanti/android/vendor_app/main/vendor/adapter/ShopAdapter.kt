package cz.quanti.android.vendor_app.main.vendor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.fragment.ProductsFragment
import cz.quanti.android.vendor_app.main.vendor.viewholder.ShopViewHolder
import cz.quanti.android.vendor_app.repository.product.dto.Product
import org.koin.core.KoinComponent
import quanti.com.kotlinlog.Log
import java.util.*
import kotlin.collections.ArrayList

class ShopAdapter(
    private val productsFragment: ProductsFragment,
    private val context: Context
) :
    RecyclerView.Adapter<ShopViewHolder>(), Filterable, KoinComponent {

    private val products: MutableList<Product> = mutableListOf()
    private val productsFull: MutableList<Product> = mutableListOf()

    private val picasso = Picasso.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ShopViewHolder(view)
    }

    fun setData(data: List<Product>) {
        products.clear()
        products.addAll(data)
        productsFull.clear()
        productsFull.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun getFilter(): Filter {
        return productFilter
    }

    private val productFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<Product> = ArrayList()
            if (constraint.isEmpty()) {
                filteredList.addAll(productsFull)
            } else {
                val filterPattern =
                    constraint.toString().toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                productsFull.forEach { product ->
                    if (product.name.toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(product)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            products.clear()
            products.addAll(results.values as List<Product>)
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.productName.text = products[position].name

        if (products[position].drawable == null ) {
            picasso.isLoggingEnabled = true
            val img = ImageView(context)
            picasso.load(products[position].image)
                .into(img, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        products[position].drawable = img.drawable
                        holder.productImage.setImageDrawable(img.drawable)
                    }

                    override fun onError(e: java.lang.Exception?) {
                        Log.e(e?.message ?: "")
                    }
                })
        } else {
            holder.productImage.setImageDrawable(products[position].drawable)
        }

        holder.productLayout.setOnClickListener {
            productsFragment.openProduct(products[position])
        }


    }
}
