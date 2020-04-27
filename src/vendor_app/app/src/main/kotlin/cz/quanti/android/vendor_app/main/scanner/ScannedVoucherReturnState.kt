package cz.quanti.android.vendor_app.main.scanner

enum class ScannedVoucherReturnState {
    VOUCHER_WITH_PASSWORD,
    VOUCHER_WITHOUT_PASSWORD,
    BOOKLET,
    WRONG_FORMAT,
    DEACTIVATED,
    WRONG_BOOKLET,
    WRONG_CURRENCY
}
