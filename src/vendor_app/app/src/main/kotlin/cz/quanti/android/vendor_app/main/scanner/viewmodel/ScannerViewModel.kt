package cz.quanti.android.vendor_app.main.scanner.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.main.scanner.ScannedVoucherReturnState
import cz.quanti.android.vendor_app.repository.booklet.BookletFacade
import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import io.reactivex.Completable
import io.reactivex.Single

class ScannerViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val bookletFacade: BookletFacade
) : ViewModel() {

    fun getDeactivatedAndProtectedBooklets(): Single<Pair<List<Booklet>, List<Booklet>>> {
        return getDeactivatedBooklets().flatMap { deactivated ->
            getProtectedBooklets().map { protected ->
                Pair(deactivated, protected)
            }
        }
    }

    private fun getDeactivatedBooklets(): Single<List<Booklet>> {
        return bookletFacade.getAllDeactivatedBooklets()
    }

    private fun getProtectedBooklets(): Single<List<Booklet>> {
        return bookletFacade.getProtectedBooklets()
    }


    fun getVoucherFromScannedCode(
        scannedCode: String,
        deactivated: List<Booklet>,
        protected: List<Booklet>
    ): Pair<Voucher?, ScannedVoucherReturnState> {
        val passwords = mutableListOf<String>()
        var bookletCode = ""
        var currency = ""
        var id: Long = 0
        var value: Long = 0
        var returnCode: ScannedVoucherReturnState
        val booklet = getBooklet()

        var regex = Regex(
            "^([A-Z$€£]+)(\\d+)\\*([\\d]+-[\\d]+-[\\d]+)-([\\d]+)-([\\dA-Z=+-/]+)$",
            RegexOption.IGNORE_CASE
        )
        var newRegex = Regex(
            "^([A-Z$€£]+)(\\d+)\\*([a-zA-Z0-9]{2,3}_.+_[0-9]{1,2}-[0-9]{1,2}-[0-9]{2,4}_batch[0-9]+)-([\\d]+)-([\\dA-Z=+-/]+)$",
            RegexOption.IGNORE_CASE
        )

        var scannedCodeInfo = regex.matchEntire(scannedCode)
        if (scannedCodeInfo == null) {
            scannedCodeInfo = newRegex.matchEntire(scannedCode)
        }
        if (scannedCodeInfo != null) {
            scannedCodeInfo.groups[5]?.value?.let { passwords.add(it) }
            returnCode = ScannedVoucherReturnState.VOUCHER_WITH_PASSWORD
        } else {
            regex = Regex(
                "^([A-Z$€£]+)(\\d+)\\*([\\d]+-[\\d]+-[\\d]+)-([\\d]+)$",
                RegexOption.IGNORE_CASE
            )
            newRegex = Regex(
                "^([A-Z$€£]+)(\\d+)\\*([a-zA-Z0-9]{2,3}_.+_[0-9]{1,2}-[0-9]{1,2}-[0-9]{2,4}_batch[0-9]+)-([\\d]+)$",
                RegexOption.IGNORE_CASE
            )

            scannedCodeInfo = regex.matchEntire(scannedCode)
            if (scannedCodeInfo == null) {
                scannedCodeInfo = newRegex.matchEntire(scannedCode)
            }
            if (scannedCodeInfo != null) {
                scannedCodeInfo.groups[3]?.value?.let { bookletCode = it }
                returnCode = ScannedVoucherReturnState.VOUCHER_WITHOUT_PASSWORD
            } else {
                regex = Regex("^([\\d]+-[\\d]+-[\\d]+)$", RegexOption.IGNORE_CASE)
                newRegex = Regex(
                    "^([a-zA-Z0-9]{2,3}_.+_[0-9]{1,2}-[0-9]{1,2}-[0-9]{2,4}_batch[0-9]+)$",
                    RegexOption.IGNORE_CASE
                )
                scannedCodeInfo = regex.matchEntire(scannedCode)
                if (scannedCodeInfo == null) {
                    scannedCodeInfo = newRegex.matchEntire(scannedCode)
                }
                return if (scannedCodeInfo != null) {
                    Pair(null, ScannedVoucherReturnState.BOOKLET)
                } else {
                    Pair(null, ScannedVoucherReturnState.WRONG_FORMAT)
                }
            }
        }

        scannedCodeInfo.groups[1]?.value?.let { currency = it }
        scannedCodeInfo.groups[2]?.value?.let { value = it.toLong() }
        scannedCodeInfo.groups[3]?.value?.let { bookletCode = it }
        scannedCodeInfo.groups[4]?.value?.let { id = it.toLong() }


        if (returnCode == ScannedVoucherReturnState.VOUCHER_WITH_PASSWORD) {
            val password = getPassword(bookletCode, protected)
            if (password != "") {
                passwords.add(password)
            }
        }

        val voucher = Voucher().apply {
            this.id = id
            this.qrCode = scannedCode
            this.booklet = bookletCode
            this.currency = currency
            this.value = value
            this.passwords = passwords
        }

        return Pair(voucher, check(voucher, returnCode, booklet, deactivated))
    }

    fun addVoucher(voucher: Voucher) {
        shoppingHolder.vouchers.add(voucher)
    }

    fun deactivate(voucher: Voucher): Completable {
        return bookletFacade.deactivate(voucher.booklet)
    }

    fun wasAlreadyScanned(code: String): Boolean {
        for (voucher in shoppingHolder.vouchers) {
            if (voucher.qrCode == code) {
                return true
            }
        }
        return false
    }

    private fun getPassword(bookletCode: String, protected: List<Booklet>): String {
        for (booklet in protected) {
            if (booklet.code == bookletCode) {
                return booklet.password
            }
        }
        return ""
    }

    private fun check(
        voucher: Voucher,
        returnCode: ScannedVoucherReturnState,
        booklet: String,
        deactivated: List<Booklet>
    ): ScannedVoucherReturnState {

        if (checkIfDeactivated(voucher, deactivated)) {
            return ScannedVoucherReturnState.DEACTIVATED
        }
        if (checkIfInvalidBooklet(voucher, booklet)) {
            return ScannedVoucherReturnState.WRONG_BOOKLET
        }
        if (checkIfDifferentCurrency(voucher, shoppingHolder.chosenCurrency)) {
            return ScannedVoucherReturnState.WRONG_CURRENCY
        }
        return returnCode
    }

    private fun getBooklet(): String {
        return if (shoppingHolder.vouchers.size > 0) {
            shoppingHolder.vouchers[0].booklet
        } else {
            ""
        }
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
}
