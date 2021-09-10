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
import cz.quanti.android.vendor_app.main.shop.fragment.ShopFragment
import cz.quanti.android.vendor_app.main.shop.viewholder.ProductViewHolder
import cz.quanti.android.vendor_app.repository.product.dto.Product
import java.util.*
import kotlin.collections.ArrayList
import org.koin.core.component.KoinComponent
import quanti.com.kotlinlog.Log

class ProductsAdapter(
    private val shopFragment: ShopFragment,
    private val context: Context
) :
    RecyclerView.Adapter<ProductViewHolder>(), KoinComponent {

    private val products: MutableList<Product> = mutableListOf()
    private val productsFull: MutableList<Product> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val productBinding = ItemProductBinding.inflate(inflater, parent, false)
        return ProductViewHolder(productBinding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredData(data: List<Product>) {
        products.clear()
        products.addAll(data)
        productsFull.clear()
        productsFull.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun filterByName(name: String) {
        productFilterByName.filter(name)
    }

    private val productFilterByName: Filter = object : Filter() {
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
                    if (product.category.name.lowercase(Locale.getDefault()).contains(filterPattern)
                        && filteredList.none { it == product }) {
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

    fun filterByCategory(category: String) {
        productFilterByCategory.filter(category)
    }

    private val productFilterByCategory: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<Product> = ArrayList()
            if (constraint.isEmpty()) {
                filteredList.addAll(productsFull)
            } else {
                val filterPattern =
                    constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                productsFull.forEach { product ->
                    if (product.category.name.lowercase(Locale.getDefault()).contains(filterPattern)) {
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

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.productName.text = products[position].name

        Glide
            .with(context)
            .load(products[position].image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.productImage)

        holder.productLayout.setOnClickListener {
            it.isEnabled = false
            Log.d(TAG, "Product $position clicked")
            shopFragment.openProduct(products[position], it)
        }
    }

    fun setData(chosenCurrency: String?, products: List<Product>?) {
        products?.filter {
            it.currency.isNullOrEmpty() || it.currency == chosenCurrency
        }?.let { filteredProducts ->
            setFilteredData(filteredProducts)
            shopFragment.filterCategories(filteredProducts.distinctBy { it.category }.map {
                it.category
            })
        }
    }

    companion object {
        private val TAG = ProductsAdapter::class.java.simpleName
    }
}
