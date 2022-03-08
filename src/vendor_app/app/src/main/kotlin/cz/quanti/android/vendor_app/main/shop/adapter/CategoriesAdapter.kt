package cz.quanti.android.vendor_app.main.shop.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.ItemCategoryBinding
import cz.quanti.android.vendor_app.main.shop.callback.CategoryAdapterCallback
import cz.quanti.android.vendor_app.main.shop.viewholder.CategoryViewHolder
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import org.koin.core.component.KoinComponent
import quanti.com.kotlinlog.Log

class CategoriesAdapter(
    private val categoryAdapterCallback: CategoryAdapterCallback,
    private val context: Context
) :
    RecyclerView.Adapter<CategoryViewHolder>(), KoinComponent {

    private val categories: MutableList<Category> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val categoryBinding = ItemCategoryBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(categoryBinding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Category>) {
        categories.clear()
        categories.addAll(data.sortedBy { it.type })
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    private fun getTintColor(type: CategoryType): Int {
        return when (type) {
            CategoryType.FOOD -> {
                ContextCompat.getColor(context, R.color.lightOrange)
            }
            CategoryType.NONFOOD -> {
                ContextCompat.getColor(context, R.color.lightBlue)
            }
            CategoryType.CASHBACK -> {
                ContextCompat.getColor(context, R.color.lightGreen)
            }
            else -> {
                ContextCompat.getColor(context, R.color.white)
            }
        }
    }

    private fun getPlaceholderImage(type: CategoryType): Drawable? {
        return when (type) {
            CategoryType.FOOD -> {
                ContextCompat.getDrawable(context, R.drawable.ic_food)
            }
            CategoryType.CASHBACK -> {
                ContextCompat.getDrawable(context, R.drawable.ic_cashback)
            }
            CategoryType.ALL -> {
                ContextCompat.getDrawable(context, R.drawable.ic_all)
            }
            else -> {
                ContextCompat.getDrawable(context, R.drawable.ic_nonfood)
            }
        }
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categories[position]
        holder.categoryName.text = item.name

        if (item.image.isNullOrEmpty()) {
            holder.categoryImage.setImageDrawable(getPlaceholderImage(item.type))
        } else {
            Glide
                .with(context)
                .load(item.image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.categoryImage)
        }
        holder.categoryForeground.background.setTint(
            ColorUtils.setAlphaComponent(
                getTintColor(item.type),
                OPACITY
            )
        )

        holder.categoryLayout.setOnClickListener {
            Log.d(TAG, "Category $item clicked")
            categoryAdapterCallback.onCategoryClicked(item)
        }
    }

    companion object {
        private val TAG = CategoriesAdapter::class.java.simpleName
        const val OPACITY = 153
    }
}
