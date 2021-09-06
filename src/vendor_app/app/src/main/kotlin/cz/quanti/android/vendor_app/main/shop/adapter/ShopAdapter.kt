package cz.quanti.android.vendor_app.main.shop.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import cz.quanti.android.vendor_app.databinding.ItemProductBinding
import cz.quanti.android.vendor_app.main.shop.fragment.ProductsFragment
import cz.quanti.android.vendor_app.main.shop.viewholder.ShopViewHolder
import cz.quanti.android.vendor_app.repository.product.dto.Product
import java.util.*
import kotlin.collections.ArrayList
import org.koin.core.component.KoinComponent
import quanti.com.kotlinlog.Log

class ShopAdapter(
    private val productsFragment: ProductsFragment,
    private val context: Context
) :
    RecyclerView.Adapter<ShopViewHolder>(), Filterable, KoinComponent {

    private val products: MutableList<Product> = mutableListOf()
    private val productsFull: MutableList<Product> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val productBinding = ItemProductBinding.inflate(inflater, parent, false)
        return ShopViewHolder(productBinding)
    }

    @SuppressLint("NotifyDataSetChanged")
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
                    constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                productsFull.forEach { product ->
                    if (product.name.lowercase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(product)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            products.clear()
            products.addAll(results.values as List<Product>)
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.productName.text = products[position].name

        Glide
            .with(context)
            .load(products[position].image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.productImage)

        holder.productLayout.setOnClickListener {
            Log.d(TAG, "Product $position clicked")
            productsFragment.openProduct(products[position])
        }
    }

    companion object {
        private val TAG = ShopAdapter::class.java.simpleName
    }
}
