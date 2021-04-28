package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class BatchApiEntity(
    var batchId: Int = 0,
    var projectId: Int = 0,
    var quantity: Int = 0,
    var date: String = "",
    var value: Long = 0,
    var currency: String = ""
)

