package cz.quanti.android.vendor_app.main.checkout.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.main.vendor.misc.CommonVariables
import cz.quanti.android.vendor_app.repository.facade.CommonFacade
import java.util.*

class CheckoutViewModel(private val facade: CommonFacade) : ViewModel() {
    // TODO handle differences in currency

    fun proceed(): Boolean {
        return if (getTotal() <= 0) {
            useVouchers()
            facade.saveVouchers(CommonVariables.vouchers)
            clear()
            true
        } else {
            false
        }
    }

    fun getTotal(): Double {
        val total = CommonVariables.cart.map { it.subTotal }.sum()
        val paid = CommonVariables.vouchers.map { it.value }.sum()
        return total - paid
    }

    private fun clear() {
        CommonVariables.cart.clear()
        CommonVariables.vouchers.clear()
    }

    private fun useVouchers() {
        for (voucher in CommonVariables.vouchers) {
            voucher.usedAt = Calendar.getInstance().time
            // TODO add vendor ID and maybe other relevant info to the voucher
            // TODO what if there are more vouchers, do all of them get the full list of products?
            voucher.productIds =
                CommonVariables.cart.map { product -> product.product.id }.distinct().toTypedArray()
        }
    }
}
