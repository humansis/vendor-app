package cz.quanti.android.vendor_app.main.checkout.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.CommonFacade
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import io.reactivex.Completable
import java.util.*

class CheckoutViewModel(private val facade: CommonFacade) : ViewModel() {
    // TODO handle differences in currency

    private var chosenCurrency: String = ""
    private var cart: MutableList<SelectedProduct> = mutableListOf()
    private var vouchers: MutableList<Voucher> = mutableListOf()

    fun init(
        chosenCurrency: String,
        cart: MutableList<SelectedProduct>,
        vouchers: MutableList<Voucher>
    ) {
        this.chosenCurrency = chosenCurrency
        this.cart = cart
        this.vouchers = vouchers
    }

    fun proceed(vouchers: List<Voucher>): Completable {
        return facade.saveVouchers(vouchers)
    }

    fun getTotal(): Double {
        val total = cart.map { it.subTotal }.sum()
        val paid = vouchers.map { it.value }.sum()
        return total - paid
    }

    private fun useVouchers() {
        for (voucher in vouchers) {
            voucher.usedAt = Calendar.getInstance().time
            // TODO add vendor ID and maybe other relevant info to the voucher
            // TODO what if there are more vouchers, do all of them get the full list of products?
            voucher.productIds =
                cart.map { product -> product.product.id }.distinct().toTypedArray()
        }
    }
}
