package cz.quanti.android.vendor_app.main.shop.callback

import android.view.View
import cz.quanti.android.vendor_app.repository.category.dto.Category
import cz.quanti.android.vendor_app.repository.product.dto.Product

interface ShopFragmentCallback {
    fun openCategory(category: Category)
    fun openProduct(product: Product, productLayout: View)
}
