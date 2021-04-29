package cz.quanti.android.vendor_app.repository.purchase.dto.api

data class V2InvoiceApiEntity (
    var totalCount: Int = 0,
    //todo napsat univerzální V2 entitu pro všechny Listy
    var data: List<InvoiceApiEntity> = listOf()
)
