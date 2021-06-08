package cz.quanti.android.vendor_app.main.checkout.viewmodel

import android.nfc.Tag
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc.dto.UserBalance
import cz.quanti.android.nfc.exception.PINException
import cz.quanti.android.nfc.exception.PINExceptionEnum
import cz.quanti.android.nfc.logger.NfcLogger
import cz.quanti.android.nfc_io_libray.types.NfcUtil
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

class CheckoutViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val purchaseFacade: PurchaseFacade,
    private val nfcFacade: VendorFacade,
    private val cardFacade: CardFacade,
    private val currentVendor: CurrentVendor,
    private val nfcTagPublisher: NfcTagPublisher
) : ViewModel() {
    private val TAG = this.javaClass.simpleName
    private var vouchers: MutableList<Voucher> = mutableListOf()
    private var pin: String? = null

    fun init() {
        this.vouchers = shoppingHolder.vouchers
    }

    fun proceed(): Completable {
        return purchaseFacade.savePurchase(createVoucherPurchase())
    }

    fun getTotal(): Double {
        val total = shoppingHolder.cart.map { it.price }.sum()
        val paid = vouchers.map { it.value }.sum()
        return round(total - paid, 3)
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

    fun updateProduct(position: Int, product: SelectedProduct) {
        shoppingHolder.cart[position] = product
    }

    fun removeFromCart(position: Int) {
        shoppingHolder.cart.removeAt(position)
    }

    fun clearCart() {
        shoppingHolder.cart.clear()
    }

    fun getCurrency(): LiveData<String> {
        return shoppingHolder.chosenCurrency
    }

    fun clearCurrency() {
        shoppingHolder.chosenCurrency.postValue("")
    }

    fun getPin(): String? {
        return pin
    }
    fun setPin(string: String?) {
        this.pin = string
    }


    fun payByCard(pin: String, value: Double, currency: String): Single<Pair<Tag, UserBalance>> {
        return subtractMoneyFromCard(pin, value, currency).flatMap { it ->
            val tag = it.first
            val userBalance = it.second
            saveCardPurchaseToDb(convertTagToString(tag))
                .subscribeOn(Schedulers.io())
                .toSingleDefault(Pair(tag, userBalance))
                .flatMap {
                    Single.just(it)
                }
        }
    }

    private fun convertTagToString(tag: Tag): String {
        return NfcUtil.toHexString(tag.id).toUpperCase(Locale.US)
    }

    private fun createVoucherPurchase(): Purchase {
        return Purchase().apply {
            products.addAll(shoppingHolder.cart)
            vouchers.addAll(shoppingHolder.vouchers.map { it.id })
            vendorId = currentVendor.vendor.id
            createdAt = convertTimeForApiRequestBody(Date())
        }
    }

    private fun subtractMoneyFromCard(
        pin: String,
        value: Double,
        currency: String
    ): Single<Pair<Tag, UserBalance>> {
        return Single.fromObservable(
            nfcTagPublisher.getTagObservable().take(1).flatMapSingle { tag ->
                cardFacade.getBlockedCards()
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                    if(it.contains(convertTagToString(tag))) {
                        throw PINException(PINExceptionEnum.CARD_LOCKED)
                    } else {
                        NfcLogger.d(
                            TAG,
                            "subtractBalanceFromCard: value: $value, currencyCode: $currency"
                        )
                        nfcFacade.subtractFromBalance(tag, pin, value, currency).map { userBalance ->
                            NfcLogger.d(
                                TAG,
                                "subtractedBalanceFromCard: balance: ${userBalance.balance}, beneficiaryId: ${userBalance.userId}, currencyCode: ${userBalance.currencyCode}"
                            )
                            Pair(tag, userBalance)
                        }
                    }
                }
            })
    }

    private fun saveCardPurchaseToDb(card: String): Completable {
        return purchaseFacade.savePurchase(createCardPurchase(card))
    }

    private fun createCardPurchase(card: String): Purchase {
        return Purchase().apply {
            products.addAll(shoppingHolder.cart)
            smartcard = card
            vendorId = currentVendor.vendor.id
            createdAt = convertTimeForApiRequestBody(Date())
        }
    }
}
