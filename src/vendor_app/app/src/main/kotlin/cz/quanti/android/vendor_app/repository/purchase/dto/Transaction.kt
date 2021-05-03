package cz.quanti.android.vendor_app.repository.purchase.dto

class Transaction (
    var projectId: Long = 0,
    var purchaseIds: List<Long> = listOf(),
    var value: Long = 0,
    var currency: String = ""
) {
    override fun equals(other: Any?): Boolean {
        return if (other is Transaction) {
            purchaseIds == other.purchaseIds
        } else {
            false
        }
    }
}
