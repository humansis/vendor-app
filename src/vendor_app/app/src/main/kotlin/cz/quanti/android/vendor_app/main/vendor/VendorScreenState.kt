package cz.quanti.android.vendor_app.main.vendor

enum class VendorScreenState(val state: Int) {
    STATE_ONLY_PRODUCTS_SHOWED(0),
    STATE_SHOPPING_CART_SHOWED(1),
    STATE_PRODUCT_DETAIL_SHOWED(2)
}
