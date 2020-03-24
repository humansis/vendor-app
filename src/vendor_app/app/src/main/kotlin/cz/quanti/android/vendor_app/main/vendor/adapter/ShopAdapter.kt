package cz.quanti.android.vendor_app.main.vendor.adapter

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.vendor.fragment.ProductDetailFragment
import cz.quanti.android.vendor_app.main.vendor.viewholder.ShopViewHolder
import cz.quanti.android.vendor_app.repository.entity.Product
import kotlinx.android.synthetic.main.fragment_product_detail.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Exception
import kotlin.math.ceil

class ShopAdapter: RecyclerView.Adapter<ShopViewHolder>(), KoinComponent {

    private val products: MutableList<Product> = mutableListOf()
    private val itemsInRow = 3
    private val picasso: Picasso by inject()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    fun setData(data: List<Product>) {
        products.clear()
        for (i in 1..10) { // TODO delete, this is here just to mock larger data
            products.addAll(data)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return ceil(products.size.toDouble() / itemsInRow).toInt()
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {

        val actualPosition = position * 3
        val productsRow = getProductsRow(holder, actualPosition)

        if(productsRow[0] != null) {
            holder.firstProductName?.text = productsRow[0]!!.name
            holder.firstProductImage?.isClickable = true
            picasso.load(productsRow[0]!!.image).networkPolicy(NetworkPolicy.OFFLINE).into(getTargetToLoadImgeIntoLayoutBackground(holder.firstProductImage))
            holder.firstProductImage?.setOnClickListener{
                selectItem(holder.itemView, productsRow[0]!!)
            }
        } else {
            holder.firstProductImage?.visibility = View.INVISIBLE
            holder.firstProductName?.visibility = View.INVISIBLE
            holder.firstProductLayout?.visibility = View.INVISIBLE
    }

        if(productsRow[1] != null) {
            holder.secondProductName?.text = productsRow[1]!!.name
            holder.secondProductImage?.isClickable = true
            picasso.load(productsRow[1]!!.image).networkPolicy(NetworkPolicy.OFFLINE).into(getTargetToLoadImgeIntoLayoutBackground(holder.secondProductImage))
            holder.secondProductImage?.setOnClickListener{
                selectItem(holder.itemView, productsRow[1]!!)
            }
        } else {
            holder.secondProductImage?.visibility = View.INVISIBLE
            holder.secondProductName?.visibility = View.INVISIBLE
            holder.secondProductLayout?.visibility = View.INVISIBLE
        }

        if(productsRow[2] != null) {
            holder.thirdProductName?.text = productsRow[2]!!.name
            holder.thirdProductImage?.isClickable = true
            picasso.load(productsRow[2]!!.image).networkPolicy(NetworkPolicy.OFFLINE).into(getTargetToLoadImgeIntoLayoutBackground(holder.thirdProductImage))
            holder.thirdProductImage?.setOnClickListener{
                selectItem(holder.itemView, productsRow[2]!!)
            }
        } else {
            holder.thirdProductImage?.visibility = View.INVISIBLE
            holder.thirdProductName?.visibility = View.INVISIBLE
            holder.thirdProductLayout?.visibility = View.INVISIBLE
        }
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

    private fun selectItem(itemView: View, product: Product) {
        val productDetailFragment = ProductDetailFragment(product)
        val transaction = (itemView.context as MainActivity).supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, productDetailFragment)
        }
        transaction.commit()
    }

    private fun getTargetToLoadImgeIntoLayoutBackground(layout: LinearLayout?): com.squareup.picasso.Target {
        return object : com.squareup.picasso.Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                layout?.background = placeHolderDrawable
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                layout?.background = errorDrawable
            }

            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                layout?.background = BitmapDrawable(layout?.context?.resources, bitmap)
            }
        }
    }
}
