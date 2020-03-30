package cz.quanti.android.vendor_app.main.checkout.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.utils.CurrentVendor
import io.reactivex.Completable
import java.util.*

class CheckoutViewModel(private val voucherFacade: VoucherFacade) : ViewModel() {
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
        useVouchers()
        return voucherFacade.saveVouchers(vouchers)
    }

    fun getTotal(): Double {
        val total = cart.map { it.subTotal }.sum()
        val paid = vouchers.map { it.value }.sum()
        return total - paid
    }

    private fun useVouchers() {
        for (voucher in vouchers) {
            voucher.usedAt = Calendar.getInstance().time
            voucher.vendorId = CurrentVendor.vendor.id
            voucher.price = cart.map { it.subTotal }.sum()
            voucher.productIds =
                cart.map { product -> product.product.id }.distinct().toTypedArray()
        }
    }
}
