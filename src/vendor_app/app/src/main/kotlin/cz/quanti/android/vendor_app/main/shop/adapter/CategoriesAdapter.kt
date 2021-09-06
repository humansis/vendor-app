package cz.quanti.android.vendor_app.main.shop.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.ItemCategoryBinding
import cz.quanti.android.vendor_app.main.shop.fragment.ShopFragment
import cz.quanti.android.vendor_app.main.shop.viewholder.CategoryViewHolder
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.repository.category.dto.CategoryType
import org.koin.core.component.KoinComponent

class CategoriesAdapter(
    private val shopFragment: ShopFragment,
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
        categories.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    private fun getTintColor(type: CategoryType): Pair<Int, Int> {
        return when (type) {
            CategoryType.FOOD -> {
                Pair(
                    ContextCompat.getColor(context, R.color.lighterOrange),
                    ContextCompat.getColor(context, R.color.lightOrange)
                )
            }
            CategoryType.NONFOOD -> {
                Pair(
                    ContextCompat.getColor(context, R.color.lighterBlue),
                    ContextCompat.getColor(context, R.color.lightBlue)
                )
            }
            CategoryType.CASHBACK -> {
                Pair(
                    ContextCompat.getColor(context, R.color.lighterGreen),
                    ContextCompat.getColor(context, R.color.lightGreen)
                )
            }
            else -> {
                Pair(
                    ContextCompat.getColor(context, R.color.white),
                    ContextCompat.getColor(context, R.color.lighterGrey)
                )
            }
        }
    }

    private fun getPlaceholderImage(type: CategoryType): Drawable? {
        return when (type) {
            CategoryType.FOOD -> {
                ContextCompat.getDrawable(context, R.drawable.ic_food)
            }
            CategoryType.NONFOOD -> {
                ContextCompat.getDrawable(context, R.drawable.ic_nonfood)
            }
            CategoryType.CASHBACK -> {
                ContextCompat.getDrawable(context, R.drawable.ic_cashback)
            }
            CategoryType.ALL -> {
                ContextCompat.getDrawable(context, R.drawable.ic_all)
            }
        }
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.categoryName.text = categories[position].name

        if (categories[position].image.isEmpty()) {
            holder.categoryImage.setImageDrawable(getPlaceholderImage(categories[position].type))
        } else {
            Glide
                .with(context)
                .load(categories[position].image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.categoryImage)
        }

        val tintColor = getTintColor(categories[position].type)
        holder.categoryLayout.background.setTint(tintColor.first)
        holder.categoryImage.drawable.setTint(tintColor.second)

        holder.categoryLayout.setOnClickListener {
            shopFragment.openCategory(categories[position])
        }
    }
}
