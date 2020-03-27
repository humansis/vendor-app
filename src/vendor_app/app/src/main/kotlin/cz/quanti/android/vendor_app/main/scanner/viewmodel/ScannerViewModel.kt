package cz.quanti.android.vendor_app.main.scanner.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher

class ScannerViewModel : ViewModel() {

    fun ifHasNoPasswordGetInfo(scannedCode: String): Voucher? {
        val passwords = mutableListOf<String>()
        var bookletCode = ""
        var currency = ""
        var id: Long = 0
        var value: Long = 0

        var regex = Regex(
            "^([A-Z$€£]+)(\\d+)\\*([\\d]+-[\\d]+-[\\d]+)-([\\d]+)-([\\dA-Z=+-/]+)$",
            RegexOption.IGNORE_CASE
        )
        var scannedCodeInfo = regex.matchEntire(scannedCode)
        if (scannedCodeInfo != null) {
            scannedCodeInfo.groups[5]?.value?.let { passwords.add(it) }
            scannedCodeInfo.groups[3]?.value?.let { bookletCode = it }
        } else {
            regex = Regex("^([\\d]+-[\\d]+-[\\d]+)\$", RegexOption.IGNORE_CASE)
            scannedCodeInfo = regex.matchEntire(scannedCode)
            if (scannedCode != null) {
                // TODO a booklet was scanned instead of voucher
                return null
            } else {
                regex = Regex(
                    "^([A-Z\$€£]+)(\\d+)\\*([\\d]+-[\\d]+-[\\d]+)-([\\d]+)\$",
                    RegexOption.IGNORE_CASE
                )
                scannedCodeInfo = regex.matchEntire(scannedCode)
                if (scannedCodeInfo != null) {
                    scannedCodeInfo.groups[3]?.value?.let { bookletCode = it }
                } else {
                    // TODO wrong format of code
                    return null
                }
            }
        }

        scannedCodeInfo.groups[1]?.value?.let { currency = it }
        scannedCodeInfo.groups[2]?.value?.let { value = it.toLong() }
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

        return if (check(voucher)) {
            voucher
        } else {
            null
        }
    }

    private fun check(voucher: Voucher): Boolean {
        // TODO check if not deactivated
        // TODO check if from same booklet
        // TODO check if of same currency
        return true
    }
}
