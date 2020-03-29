package cz.quanti.android.vendor_app.main.scanner.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher

class ScannerViewModel : ViewModel() {

    fun getVoucherFromScannedCode(scannedCode: String): Pair<Voucher?, Int> {
        val passwords = mutableListOf<String>()
        var bookletCode = ""
        var currency = ""
        var id: Long = 0
        var value: Long = 0
        var returnCode: Int

        var regex = Regex(
            "^([A-Z$€£]+)(\\d+)\\*([\\d]+-[\\d]+-[\\d]+)-([\\d]+)-([\\dA-Z=+-/]+)$",
            RegexOption.IGNORE_CASE
        )
        var scannedCodeInfo = regex.matchEntire(scannedCode)
        if (scannedCodeInfo != null) {
            scannedCodeInfo.groups[5]?.value?.let { passwords.add(it) }
            returnCode = VOUCHER_WITH_PASSWORD
        } else {
            regex = Regex(
                "^([A-Z$€£]+)(\\d+)\\*([\\d]+-[\\d]+-[\\d]+)-([\\d]+)$",
                RegexOption.IGNORE_CASE
            )
            scannedCodeInfo = regex.matchEntire(scannedCode)
            if (scannedCodeInfo != null) {
                scannedCodeInfo.groups[3]?.value?.let { bookletCode = it }
                returnCode = VOUCHER_WITHOUT_PASSWORD
            } else {
                regex = Regex("^([\\d]+-[\\d]+-[\\d]+)$", RegexOption.IGNORE_CASE)
                scannedCodeInfo = regex.matchEntire(scannedCode)
                return if (scannedCodeInfo != null) {
                    Pair(null, BOOKLET)
                } else {
                    Pair(null, WRONG_FORMAT)
                }
            }
        }

        scannedCodeInfo.groups[1]?.value?.let { currency = it }
        scannedCodeInfo.groups[2]?.value?.let { value = it.toLong() }
        scannedCodeInfo.groups[3]?.value?.let { bookletCode = it }
        scannedCodeInfo.groups[4]?.value?.let { id = it.toLong() }

        // TODO handle protected booklets and passwords

        val voucher = Voucher().apply {
            this.id = id
            this.qrCode = scannedCode
            this.booklet = bookletCode
            this.currency = currency
            this.value = value
            // TODO set vendor id, product ids, price and date (the checkout part will take care of that?)
        }

        return Pair(voucher, check(voucher, returnCode))
    }

    private fun check(voucher: Voucher, returnCode: Int): Int {
        var newReturnCode = returnCode
        // TODO check if not deactivated
        // TODO check if from same booklet
        // TODO check if of same currency
        return newReturnCode
    }

    companion object {
        const val VOUCHER_WITH_PASSWORD = 1
        const val VOUCHER_WITHOUT_PASSWORD = 2
        const val BOOKLET = 3
        const val WRONG_FORMAT = 4
    }
}
