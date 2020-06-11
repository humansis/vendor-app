package cz.quanti.android.vendor_app.main.checkout.viewmodel

import androidx.lifecycle.ViewModel
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc.dto.UserBalance
import cz.quanti.android.vendor_app.main.checkout.CheckoutScreenState
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.card.dto.CardPayment
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class CheckoutViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val voucherFacade: VoucherFacade,
    private val cardFacade: CardFacade,
    private val nfcFacade: VendorFacade,
    private val currentVendor: CurrentVendor,
    private val nfcTagPublisher: NfcTagPublisher
) : ViewModel() {
    private var vouchers: MutableList<Voucher> = mutableListOf()

    fun init() {
        this.vouchers = shoppingHolder.vouchers
    }

    fun proceed(): Completable {
        useVouchers()
        return voucherFacade.saveVouchers(vouchers)
    }

    fun getTotal(): Double {
        val total = shoppingHolder.cart.map { it.subTotal }.sum()
        val paid = vouchers.map { it.value }.sum()
        return total - paid
    }

    fun getVouchers(): List<Voucher> {
        return vouchers
    }

    fun getShoppingCart(): List<SelectedProduct> {
        return shoppingHolder.cart
    }

    fun clearVouchers() {
        vouchers.clear()
    }

    fun clearShoppingCart() {
        shoppingHolder.cart.clear()
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

    fun payByCard(pin: String, value: Double): Observable<UserBalance> {
        return subtractMoneyFromCard(pin, value).flatMapSingle { userBalance ->
            saveCardPaymentsToDb(userBalance.userId).subscribeOn(Schedulers.io())
                .toSingleDefault(userBalance)
        }
    }

    private fun subtractMoneyFromCard(pin: String, value: Double): Observable<UserBalance> {
        return nfcTagPublisher.getTagObservable().take(1).flatMapSingle {
            nfcFacade.subtractFromBalance(it, pin, value)
        }
    }

    private fun saveCardPaymentsToDb(card: String): Completable {
        val payments: MutableList<CardPayment> = mutableListOf()
        val time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
            .format(Date())
        shoppingHolder.cart.forEach { item ->
            val payment = CardPayment().apply {
                cardId = card
                productId = item.product.id
                value = item.subTotal
                quantity = item.quantity
                createdAt = time
            }
            payments.add(payment)
        }
        return Observable.fromIterable(payments).flatMapCompletable { payment ->
            cardFacade.saveCardPayment(payment)
        }
    }

    private fun useVouchers() {
        for (voucher in vouchers) {
            voucher.usedAt = Calendar.getInstance().time
            voucher.vendorId = currentVendor.vendor.id
            voucher.price = cart.map { it.subTotal }.sum()
            //TODO handle productIds
        }
    }
}
