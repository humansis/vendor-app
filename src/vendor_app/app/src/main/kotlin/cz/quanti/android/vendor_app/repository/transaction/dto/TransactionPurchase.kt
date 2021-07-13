package cz.quanti.android.vendor_app.repository.transaction.dto

class TransactionPurchase (
    var purchaseId: Long = 0,
    var value: Double = 0.0,
    var currency: String = "",
    var beneficiaryId: Long = 0,
    var createdAt: String = "",
    var transactionId: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        return if (other is TransactionPurchase) {
            purchaseId == other.purchaseId
        } else {
            false
        }
    }
}
