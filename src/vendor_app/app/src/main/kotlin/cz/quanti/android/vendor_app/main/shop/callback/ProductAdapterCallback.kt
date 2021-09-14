package cz.quanti.android.vendor_app.main.shop.callback

import cz.quanti.android.vendor_app.repository.product.dto.Product

interface ProductAdapterCallback {
    fun onProductClicked(product: Product)
}
