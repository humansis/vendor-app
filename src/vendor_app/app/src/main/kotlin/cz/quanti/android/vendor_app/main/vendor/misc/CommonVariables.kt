package cz.quanti.android.vendor_app.main.vendor.misc

import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher

object CommonVariables {
    var choosenCurrency: String = ""
    val cart: MutableList<SelectedProduct> = mutableListOf()
    val vouchers: MutableList<Voucher> = mutableListOf()
}
