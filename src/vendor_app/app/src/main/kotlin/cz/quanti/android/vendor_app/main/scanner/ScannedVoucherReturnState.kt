package cz.quanti.android.vendor_app.main.scanner

enum class ScannedVoucherReturnState(state: Int) {
    VOUCHER_WITH_PASSWORD(1),
    VOUCHER_WITHOUT_PASSWORD(2),
    BOOKLET(3),
    WRONG_FORMAT(4),
    DEACTIVATED(5),
    WRONG_BOOKLET(6),
    WRONG_CURRENCY(7)
}
