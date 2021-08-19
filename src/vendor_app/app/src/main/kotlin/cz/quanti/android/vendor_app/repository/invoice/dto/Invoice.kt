package cz.quanti.android.vendor_app.repository.invoice.dto

class Invoice(
    var invoiceId: Long = 0,
    var quantity: Int = 0,
    var date: String = "",
    var value: Double = 0.0,
    var currency: String = ""
) {
    override fun equals(other: Any?): Boolean {
        return if (other is Invoice) {
            invoiceId == other.invoiceId
        } else {
            false
        }
    }
}
