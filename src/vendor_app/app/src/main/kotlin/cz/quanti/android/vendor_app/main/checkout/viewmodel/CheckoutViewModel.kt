package cz.quanti.android.vendor_app.main.checkout.viewmodel

import android.nfc.Tag
import android.os.CpuUsageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import cz.quanti.android.vendor_app.repository.purchase.dto.PurchasedProduct
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import quanti.com.kotlinlog.Log
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
    private var pin: String? = null
    private var originalCardData = OriginalCardData(null, null)
    private val paymentStateLD = MutableLiveData<Pair<PaymentStateEnum, PaymentResult?>>(Pair(PaymentStateEnum.READY, null))

    fun init() {
        this.vouchers = shoppingHolder.vouchers
    }

    fun proceed(): Completable {
        return saveVoucherPurchaseToDb()
    }

    fun getTotal(): Double {
        val total = shoppingHolder.cart.map { it.price }.sum()
        val paid = vouchers.map { it.value }.sum()
        return round(total - paid, 3)
    }

    fun setPaymentState(paymentState: PaymentStateEnum) {
        this.paymentStateLD.postValue(Pair(paymentState, null))
    }

    private fun setPaymentState(paymentState: PaymentStateEnum, result: PaymentResult) {
        this.paymentStateLD.postValue(Pair(paymentState, result))
    }

    fun getPaymentState(): MutableLiveData<Pair<PaymentStateEnum, PaymentResult?>> {
        return paymentStateLD
    }

    fun setOriginalCardData(originalBalance: Double?, originalTagId: ByteArray?) {
        this.originalCardData = OriginalCardData(originalBalance, originalTagId)
    }

    fun getOriginalCardData(): OriginalCardData {
        return originalCardData
    }

    fun getVouchers(): List<Voucher> {
        return vouchers
    }

    fun clearVouchers() {
        vouchers.clear()
    }

    fun getSelectedProductsLD(): LiveData<List<SelectedProduct>> {
        return shoppingHolder.getProductsLD()
    }

    fun setProducts(products: List<SelectedProduct>) {
        shoppingHolder.cart.clear()
        shoppingHolder.cart.addAll(products)
    }

    fun updateSelectedProduct(product: SelectedProduct): Completable {
        return shoppingHolder.updateProduct(product)
    }

    fun removeFromCart(product: SelectedProduct): Completable {
        return shoppingHolder.removeProductAt(product)
    }

    fun clearCart(): Completable {
        return shoppingHolder.removeAllProducts()
    }

    fun getCurrencyObservable(): Observable<String> {
        return shoppingHolder.currency
    }

    fun getCurrency(): String? {
        return shoppingHolder.currency.value
    }

    fun getPin(): String? {
        return pin
    }
    fun setPin(string: String?) {
        this.pin = string
    }

    fun payByCard(pin: String): Disposable {
        return subtractMoneyFromCard(pin, getTotal(), getCurrency().toString()).flatMap {
            val tag = it.first
            val userBalance = it.second
            saveCardPurchaseToDb(convertTagToString(tag), userBalance)
                .subscribeOn(Schedulers.io())
                .toSingleDefault(userBalance)
                .flatMap {
                    Single.just(userBalance)
                }
        }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            setPaymentState(
                PaymentStateEnum.SUCCESS,
                PaymentResult(userBalance = it)
            )
        }, {
            setPaymentState(
                PaymentStateEnum.FAILED,
                PaymentResult(throwable = it)
            )
        })
    }

    private fun convertTagToString(tag: Tag): String {
        return NfcUtil.toHexString(tag.id).uppercase(Locale.US)
    }

    private fun subtractMoneyFromCard(
        pin: String,
        value: Double,
        currency: String
    ): Single<Pair<Tag, UserBalance>> {
        return Single.fromObservable(
            nfcTagPublisher.getTagObservable().take(1).flatMapSingle { tag ->
                setPaymentState(PaymentStateEnum.IN_PROGRESS)
                cardFacade.getBlockedCards()
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        if(it.contains(convertTagToString(tag))) {
                            throw PINException(PINExceptionEnum.CARD_LOCKED, tag.id)
                        } else {
                            NfcLogger.d(
                                TAG,
                                "subtractBalanceFromCard: value: $value, currencyCode: $currency, originalBalance: ${originalCardData.balance}"
                            )
                            if (originalCardData.tagId == null || originalCardData.tagId.contentEquals( tag.id )) {
                                nfcFacade.subtractFromBalance(tag, pin, value, currency, originalCardData.balance).map { userBalance ->
                                    NfcLogger.d(
                                        TAG,
                                        "subtractedBalanceFromCard: balance: ${userBalance.balance}, beneficiaryId: ${userBalance.userId}, currencyCode: ${userBalance.currencyCode}"
                                    )
                                    Pair(tag, userBalance)
                                }
                            } else {
                                throw PINException(PINExceptionEnum.INVALID_DATA, tag.id)
                            }
                        }
                }
        })
    }

    private fun saveVoucherPurchaseToDb(): Completable {
        return purchaseFacade.savePurchase(createVoucherPurchase())
    }

    private fun createVoucherPurchase(): Purchase {
        return Purchase().apply {
            products.addAll(convert(shoppingHolder.cart))
            vouchers.addAll(shoppingHolder.vouchers.map { it.id })
            vendorId = currentVendor.vendor.id
            createdAt = convertTimeForApiRequestBody(Date())
            currency = getCurrency().toString()
        }
    }

    private fun saveCardPurchaseToDb(card: String, userBalance: UserBalance): Completable {
        return purchaseFacade.savePurchase(createCardPurchase(card, userBalance))
    }

    private fun createCardPurchase(card: String, userBalance: UserBalance): Purchase {
        return Purchase().apply {
            products.addAll(convert(shoppingHolder.cart))
            smartcard = card
            beneficiaryId = userBalance.userId.toLong()
            vendorId = currentVendor.vendor.id
            createdAt = convertTimeForApiRequestBody(Date())
            currency = userBalance.currencyCode
        }
    }

    private fun convert(cart: MutableList<SelectedProduct>): Collection<PurchasedProduct> {
        return cart.map {
            PurchasedProduct (
                product = it.product,
                price = it.price
            )
        }
    }

    class PaymentResult(
        val userBalance: UserBalance? = null,
        val throwable: Throwable? = null
    )

    enum class PaymentStateEnum {
        READY,
        IN_PROGRESS,
        SUCCESS,
        FAILED
    }

    companion object {
        private val TAG = CheckoutViewModel::class.java.simpleName
    }
}
