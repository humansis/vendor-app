package cz.quanti.android.vendor_app.repository.transaction.dto.api

data class TransactionApiEntity(
    var projectId: Long = 0,
    var value: Double = 0.0,
    var currency: String = ""
)
