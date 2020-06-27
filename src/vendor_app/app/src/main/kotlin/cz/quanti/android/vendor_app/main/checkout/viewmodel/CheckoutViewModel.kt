package cz.quanti.android.vendor_app.main.checkout.viewmodel

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc.dto.UserBalance
import cz.quanti.android.vendor_app.main.checkout.CheckoutScreenState
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.card.dto.CardPayment
import cz.quanti.android.vendor_app.repository.product.dto.SelectedProduct
import cz.quanti.android.vendor_app.repository.voucher.VoucherFacade
import cz.quanti.android.vendor_app.repository.voucher.dto.Voucher
import cz.quanti.android.vendor_app.repository.voucher.dto.VoucherPurchase
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import cz.quanti.android.vendor_app.utils.convertTimeForApiRequestBody
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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
        val purchase = createVoucherPurchase()
        return voucherFacade.saveVoucherPurchase(purchase)
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

    fun payByCard(pin: String, value: Double): Single<Pair<Tag, UserBalance>> {
        return subtractMoneyFromCard(pin, value).flatMap {
            val tag = it.first
            val userBalance = it.second
            saveCardPaymentsToDb(
                tag.id.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') })
                .subscribeOn(Schedulers.io())
                .toSingleDefault(Pair(tag, userBalance))
                .flatMap {
                    Single.just(it)
                }
        }
    }

    private fun createVoucherPurchase(): VoucherPurchase {
        return VoucherPurchase().apply {
            products.addAll(shoppingHolder.cart)
            vouchers.addAll(shoppingHolder.vouchers.map { it.id })
            vendorId = currentVendor.vendor.id
            createdAt = convertTimeForApiRequestBody(Date())
        }
    }

    private fun subtractMoneyFromCard(pin: String, value: Double): Single<Pair<Tag, UserBalance>> {
        return Single.fromObservable(
            nfcTagPublisher.getTagObservable().take(1).flatMapSingle { tag ->
                nfcFacade.subtractFromBalance(tag, pin, value).flatMap { userBalance ->
                    Single.just(Pair(tag, userBalance))
                }
            })
    }

    private fun saveCardPaymentsToDb(card: String): Completable {
        val payments: MutableList<CardPayment> = mutableListOf()
        val time = convertTimeForApiRequestBody(Date())
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
}
