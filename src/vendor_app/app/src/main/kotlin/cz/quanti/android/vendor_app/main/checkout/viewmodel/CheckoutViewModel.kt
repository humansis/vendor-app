package cz.quanti.android.vendor_app.main.checkout.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.vendor_app.main.checkout.CheckoutScreenState
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import io.reactivex.Completable
import java.util.*

class CheckoutViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val voucherFacade: VoucherFacade
) : ViewModel() {
    private var cart: MutableList<SelectedProduct> = mutableListOf()
    private var vouchers: MutableList<Voucher> = mutableListOf()

    fun init() {
        this.cart = shoppingHolder.cart
        this.vouchers = shoppingHolder.vouchers
    }

    fun proceed(): Completable {
        useVouchers()
        return voucherFacade.saveVouchers(vouchers)
    }

    fun getTotal(): Double {
        val total = cart.map { it.subTotal }.sum()
        val paid = vouchers.map { it.value }.sum()
        return total - paid
    }

    fun getVouchers(): List<Voucher> {
        return vouchers
    }

    fun getShoppingCart(): List<SelectedProduct> {
        return cart
    }

    fun clearVouchers() {
        vouchers.clear()
    }

    fun clearShoppingCart() {
        cart.clear()
    }

    fun setScreenState(state: CheckoutScreenState) {
        shoppingHolder.checkoutScreenState = state
    }

    fun getScreenState(): CheckoutScreenState {
        return shoppingHolder.checkoutScreenState
    }

    fun getCurrency(): String {
        return shoppingHolder.chosenCurrency
    }

    fun clearCurrency() {
        shoppingHolder.chosenCurrency = ""
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
