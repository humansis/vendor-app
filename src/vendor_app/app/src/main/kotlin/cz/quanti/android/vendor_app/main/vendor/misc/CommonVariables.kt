package cz.quanti.android.vendor_app.main.vendor.misc

import cz.quanti.android.vendor_app.repository.entity.SelectedProduct
import cz.quanti.android.vendor_app.repository.entity.Voucher

object CommonVariables {
    var choosenCurrency: String = ""
    val cart: MutableList<SelectedProduct> = mutableListOf()
    val vouchers: MutableList<Voucher> = mutableListOf()
}
