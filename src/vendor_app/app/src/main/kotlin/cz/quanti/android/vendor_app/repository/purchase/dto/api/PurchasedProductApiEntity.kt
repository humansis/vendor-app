package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class PurchasedProductApiEntity(
    var id: Long = 0,
    var quantity: Double = 1.0, // TODO delete once its possible
    var value: Double = 0.0,
    var currency: String? = null
)
