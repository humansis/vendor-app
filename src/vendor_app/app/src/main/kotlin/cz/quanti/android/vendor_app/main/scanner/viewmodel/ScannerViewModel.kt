package cz.quanti.android.vendor_app.main.scanner.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.repository.voucher.dto.Booklet
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import io.reactivex.Single

class ScannerViewModel(private val voucherFacade: VoucherFacade) : ViewModel() {

    fun getDeactivatedBooklets(): Single<List<Booklet>> {
        return voucherFacade.getDeactivatedBooklets()
    }

    fun getVoucherFromScannedCode(
        scannedCode: String,
        chosenCurrency: String,
        booklet: String,
        deactivated: List<Booklet>
    ): Pair<Voucher?, Int> {
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
        }

        return Pair(voucher, check(voucher, returnCode, chosenCurrency, booklet, deactivated))
    }

    private fun check(
        voucher: Voucher,
        returnCode: Int,
        validCurrency: String,
        booklet: String,
        deactivated: List<Booklet>
    ): Int {

        if (checkIfDeactivated(voucher, deactivated)) {
            return DEACTIVATED
        }
        if (checkIfInvalidBooklet(voucher, booklet)) {
            return WRONG_BOOKLET
        }
        if (checkIfDifferentCurrency(voucher, validCurrency)) {
            return WRONG_CURRENCY
        }
        return returnCode
    }

    private fun checkIfDeactivated(voucher: Voucher, deactivated: List<Booklet>): Boolean {
        return deactivated.map { booklet -> booklet.code }.contains(voucher.booklet)
    }

    private fun checkIfInvalidBooklet(voucher: Voucher, booklet: String): Boolean {
        return if (booklet == "") {
            false
        } else {
            voucher.booklet != booklet
        }
    }

    private fun checkIfDifferentCurrency(voucher: Voucher, validCurrency: String): Boolean {
        return voucher.currency != validCurrency
    }

    companion object {
        const val VOUCHER_WITH_PASSWORD = 1
        const val VOUCHER_WITHOUT_PASSWORD = 2
        const val BOOKLET = 3
        const val WRONG_FORMAT = 4
        const val DEACTIVATED = 5
        const val WRONG_BOOKLET = 6
        const val WRONG_CURRENCY = 7
    }
}
