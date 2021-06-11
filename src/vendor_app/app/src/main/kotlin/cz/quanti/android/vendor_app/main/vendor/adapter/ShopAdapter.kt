package cz.quanti.android.vendor_app.main.vendor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.viewholder.ShopViewHolder
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import cz.quanti.android.vendor_app.repository.product.dto.Product
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import org.koin.core.KoinComponent
import quanti.com.kotlinlog.Log
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil

class ShopAdapter(
    private val vm: VendorViewModel,
    private val context: Context
) :
    RecyclerView.Adapter<ShopViewHolder>(), Filterable, KoinComponent {

    private val products: MutableList<Product> = mutableListOf()
    private val productsFull: MutableList<Product> = mutableListOf()

    var chosenCurrency: String = ""
    private var expandedCardHolder: ShopViewHolder? = null
    private var firstNeighbor: View? = null
    private var secondNeighbor: View? = null

    private val itemsInRow = 3
    private val picasso = Picasso.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
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
        return ceil(products.size.toDouble() / itemsInRow).toInt()
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

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            products.clear()
            products.addAll(results.values as List<Product>) // todo vyresit
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {

        val actualPosition = position * 3
        val productsRow = getProductsRow(actualPosition)

        if (productsRow[0] != null) {
            holder.firstProductName?.text = productsRow[0]?.name
            holder.firstProductImage?.isClickable = true
            picasso.isLoggingEnabled = true
            val img = ImageView(context)
            picasso.load(productsRow[0]?.image)
                .into(img, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        holder.firstProductImage?.background = img.drawable
                    }

                    override fun onError(e: java.lang.Exception?) {
                        Log.e(e?.message ?: "")
                    }
                })
            holder.firstProductLayout?.setOnClickListener {  // todo vyresit na co vlastne klikat... tady se kůlika na layout, u dalsich 2 produktu se klika na image
                productsRow[0]?.let { product -> expandCard(
                    holder,
                    product,
                    0,
                    holder.secondProduct,
                    holder.thirdProduct
                ) }
            }
        } else {
            holder.firstProductImage?.visibility = View.INVISIBLE
            holder.firstProductName?.visibility = View.INVISIBLE
            holder.firstProductLayout?.visibility = View.INVISIBLE
        }

        if (productsRow[1] != null) {
            holder.secondProductName?.text = productsRow[1]?.name
            holder.secondProductImage?.isClickable = true
            val img = ImageView(context)
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
                productsRow[1]?.let { product -> expandCard(
                    holder,
                    product,
                    1,
                    holder.firstProduct,
                    holder.thirdProduct
                ) }
            }
        } else {
            holder.secondProductImage?.visibility = View.INVISIBLE
            holder.secondProductName?.visibility = View.INVISIBLE
            holder.secondProductLayout?.visibility = View.INVISIBLE
        }

        if (productsRow[2] != null) {
            holder.thirdProductName?.text = productsRow[2]?.name
            holder.thirdProductImage?.isClickable = true
            val img = ImageView(context)
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
                productsRow[2]?.let { product -> expandCard(
                    holder,
                    product,
                    2,
                    holder.firstProduct,
                    holder.secondProduct
                ) }
            }
        } else {
            holder.thirdProductImage?.visibility = View.INVISIBLE
            holder.thirdProductName?.visibility = View.INVISIBLE
            holder.thirdProductLayout?.visibility = View.INVISIBLE
        }
    }

    private fun getProductsRow(position: Int): Array<Product?> {
        val productsRow = Array<Product?>(3) { null }

        for (i in 0..2) {
            if (products.size > position + i) {
                productsRow[i] = products[position + i]
            }
        }

        return productsRow
    }

    private fun expandCard(
        holder: ShopViewHolder,
        product: Product,
        position: Int,
        firstNeighbor: View?,
        secondNeighbor: View?
    ) {
        if (expandedCardHolder != holder) {
            closeExpandedCard()
            expandedCardHolder = holder

            this.firstNeighbor = firstNeighbor
            this.secondNeighbor = secondNeighbor
            firstNeighbor?.visibility = View.GONE
            secondNeighbor?.visibility = View.GONE

            holder.firstProductPaddingLeft?.visibility = View.VISIBLE
            holder.firstProductPaddingRight?.visibility = View.VISIBLE
            holder.firstProductOptions?.visibility = View.VISIBLE

            loadOptions(holder, product)
        }
    }

    private fun loadOptions(holder: ShopViewHolder, product: Product) {
        holder.firstProductPriceEditText?.hint = context.getString(R.string.price)
        holder.firstProductPriceTextInputLayout?.suffixText = chosenCurrency
        holder.firstProductConfirmButton?.text = context.getString(R.string.add_to_cart)

        holder.firstProductCloseButton?.setOnClickListener {
            closeCard(holder)
            // todo zavrit po kliku klavesnici
        }
        holder.firstProductConfirmButton?.setOnClickListener {
            try {
                val price = holder.firstProductPriceEditText?.text.toString().toDouble()
                if (price <= 0.0) {
                    showInvalidPriceEnteredMessage()
                } else {
                    addProductToCart(
                        product,
                        price
                    )
                    closeCard(holder)
                }
            } catch(e: NumberFormatException) {
                showInvalidPriceEnteredMessage()
            }
        }
    }

    private fun closeCard(holder: ShopViewHolder) {
        this.firstNeighbor?.visibility = View.VISIBLE
        this.secondNeighbor?.visibility = View.VISIBLE
        this.firstNeighbor = null
        this.secondNeighbor = null
        holder.firstProductPaddingLeft?.visibility = View.GONE
        holder.firstProductPaddingRight?.visibility = View.GONE
        holder.firstProductOptions?.visibility = View.GONE
        expandedCardHolder = null
    }

    fun closeExpandedCard(): Boolean {
        expandedCardHolder?.let {
            closeCard(it)
            return true
        } ?: return false
    }

    private fun addProductToCart(product: Product, unitPrice: Double) {
        val selected = SelectedProduct()
            .apply {
                this.product = product
                this.price = unitPrice
                this.currency = vm.getCurrency().value.toString()
            }
        vm.addToShoppingCart(selected)
    }

    private fun showInvalidPriceEnteredMessage() {
        Toast.makeText(
            context,
            context.getString(R.string.please_enter_price),
            Toast.LENGTH_LONG
        ).show()
    }
}
