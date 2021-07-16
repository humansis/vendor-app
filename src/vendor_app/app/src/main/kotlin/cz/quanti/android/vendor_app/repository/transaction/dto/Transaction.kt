package cz.quanti.android.vendor_app.repository.transaction.dto

class Transaction (
    var projectId: Long = 0,
    var purchases: List<TransactionPurchase> = listOf(),
    var value: Double = 0.0,
    var currency: String = ""
) {
    override fun equals(other: Any?): Boolean {
        return if (other is Transaction) {
            purchases == other.purchases
        } else {
            false
        }
    }
}
