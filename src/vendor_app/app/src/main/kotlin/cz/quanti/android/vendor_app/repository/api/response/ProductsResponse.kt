package cz.quanti.android.vendor_app.repository.api.response

import cz.quanti.android.vendor_app.repository.entity.Product

data class ProductsResponse(
    var products: Array<Product>
)
