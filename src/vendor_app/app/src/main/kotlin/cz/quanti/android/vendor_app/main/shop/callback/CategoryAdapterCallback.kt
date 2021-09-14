package cz.quanti.android.vendor_app.main.shop.callback

import cz.quanti.android.vendor_app.repository.category.dto.Category

interface CategoryAdapterCallback {
    fun onCategoryClicked(category: Category)
}
