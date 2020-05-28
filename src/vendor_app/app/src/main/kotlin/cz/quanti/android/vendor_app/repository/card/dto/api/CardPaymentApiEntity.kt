package cz.quanti.android.vendor_app.repository.card.dto.api

data class CardPaymentApiEntity(
    var productId: Long = 0,
    var value: Double = 0.0,
    var quantity: Double = 0.0,
    var createdAt: String = ""
)
