package cz.quanti.android.vendor_app.repository.card.dto

data class CardPayment(
    var cardId: String = "",
    var productId: Long = 0,
    var value: Long = 0,
    var createdAt: String = ""
)
