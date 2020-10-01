package cz.quanti.android.vendor_app.main.checkout.viewmodel

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc.dto.UserBalance
import cz.quanti.android.nfc.exception.PINException
import cz.quanti.android.nfc.exception.PINExceptionEnum
import cz.quanti.android.nfc_io_libray.types.NfcUtil
import cz.quanti.android.vendor_app.main.checkout.CheckoutScreenState
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import cz.quanti.android.vendor_app.utils.convertTimeForApiRequestBody
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
    private var vouchers: MutableList<Voucher> = mutableListOf()

    fun init() {
        this.vouchers = shoppingHolder.vouchers
    }

    fun proceed(): Completable {
        return purchaseFacade.savePurchase(createVoucherPurchase())
    }

    fun getTotal(): Double {
        val total = shoppingHolder.cart.map { it.price }.sum()
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

    fun getCurrency(): String {
        return shoppingHolder.chosenCurrency
    }

    fun clearCurrency() {
        shoppingHolder.chosenCurrency = ""
    }

    fun payByCard(pin: String, value: Double, currency: String): Single<Pair<Tag, UserBalance>> {
        return subtractMoneyFromCard(pin, value, currency).flatMap {
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
                        nfcFacade.subtractFromBalance(tag, pin, value, currency).map { userBalance ->
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
