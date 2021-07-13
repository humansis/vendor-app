package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class V2InvoiceApiEntity (
    var totalCount: Int = 0,
    var data: List<InvoiceApiEntity> = listOf()
)

data class InvoiceApiEntity(
    var id: Long = 0,
    var projectId: Int = 0,
    var quantity: Int = 0,
    var date: String = "",
    var value: Double = 0.0,
    var currency: String = ""
)
