package cz.quanti.android.vendor_app.main.checkout.viewmodel

import android.nfc.Tag
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.quanti.android.nfc.VendorFacade
import cz.quanti.android.nfc.dto.v2.PreserveBalance
import cz.quanti.android.nfc.dto.v2.UserBalance
import cz.quanti.android.nfc.exception.PINException
import cz.quanti.android.nfc.exception.PINExceptionEnum
import cz.quanti.android.nfc.logger.NfcLogger
import cz.quanti.android.vendor_app.repository.booklet.dto.Voucher
import cz.quanti.android.vendor_app.repository.card.CardFacade
import cz.quanti.android.vendor_app.repository.deposit.DepositFacade
import cz.quanti.android.vendor_app.repository.purchase.PurchaseFacade
import cz.quanti.android.vendor_app.repository.purchase.dto.Purchase
import cz.quanti.android.vendor_app.repository.purchase.dto.PurchasedProduct
import cz.quanti.android.vendor_app.repository.purchase.dto.SelectedProduct
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.NfcTagPublisher
import cz.quanti.android.vendor_app.utils.OriginalCardData
import cz.quanti.android.vendor_app.utils.ShoppingHolder
import cz.quanti.android.vendor_app.utils.SingleLiveEvent
import cz.quanti.android.vendor_app.utils.convertTagToString
import cz.quanti.android.vendor_app.utils.convertTimeForApiRequestBody
import cz.quanti.android.vendor_app.utils.round
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.Date

class CheckoutViewModel(
    private val shoppingHolder: ShoppingHolder,
    private val purchaseFacade: PurchaseFacade,
    private val nfcFacade: VendorFacade,
    private val cardFacade: CardFacade,
    private val depositFacade: DepositFacade,
    private val currentVendor: CurrentVendor,
    private val nfcTagPublisher: NfcTagPublisher
) : ViewModel() {
    private var vouchers: MutableList<Voucher> = mutableListOf()
    private var pin: String? = null
    private var originalCardData = OriginalCardData(null, null)
    private val paymentStateLD =
        MutableLiveData<Pair<PaymentStateEnum, PaymentResult?>>(Pair(PaymentStateEnum.READY, null))
    private val limitExceededSLE = SingleLiveEvent<Map<Int, Double>>()

    fun init() {
        this.vouchers = shoppingHolder.vouchers
    }

    fun proceed(): Completable {
        return saveVoucherPurchaseToDb()
    }

    fun getTotal(): Double {
        val total = shoppingHolder.cart.map { it.price }.sum()
        val paid = vouchers.map { it.value }.sum()
        return round(total - paid, 2)
    }

    private fun getAmounts(): Map<Int, Double> {
        val amounts = mutableMapOf<Int, Double>()
        shoppingHolder.cart.forEach { selectedProduct ->
            val typeId = selectedProduct.product.category.type.typeId
            amounts[typeId] = amounts[typeId]?.plus(selectedProduct.price) ?: selectedProduct.price
        }
        return amounts
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

    fun setOriginalCardData(preserveBalance: PreserveBalance?, originalTagId: ByteArray?) {
        this.originalCardData = OriginalCardData(preserveBalance, originalTagId)
    }

    fun getOriginalCardData(): OriginalCardData {
        return originalCardData
    }

    fun setLimitsExceeded(limitsExceeded: Map<Int, Double>) {
        limitExceededSLE.value = limitsExceeded
    }

    fun getLimitsExceeded(): SingleLiveEvent<Map<Int, Double>> {
        return limitExceededSLE
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
        return shoppingHolder.removeProduct(product)
    }

    fun removeFromCartByTypes(typesToRemove: Set<Int>): Completable {
        return shoppingHolder.removeProductsByType(typesToRemove)
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
        return subtractMoneyFromCard(pin, getAmounts(), getCurrency().toString()).flatMap {
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

    private fun subtractMoneyFromCard(
        pin: String,
        amounts: Map<Int, Double>,
        currency: String
    ): Single<Pair<Tag, UserBalance>> {
        return Single.fromObservable(
            nfcTagPublisher.getTagObservable().take(1).flatMapSingle { tag ->
                setPaymentState(PaymentStateEnum.IN_PROGRESS)
                cardFacade.getBlockedCards()
                    .subscribeOn(Schedulers.io())
                    .flatMap { blockedCards ->
                        if (blockedCards.contains(convertTagToString(tag))) {
                            throw PINException(PINExceptionEnum.CARD_LOCKED, tag.id)
                        } else {
                            depositFacade.getRelevantReliefPackage(convertTagToString(tag))
                                .subscribeOn(Schedulers.io())
                                .flatMap { wrappedReliefPackage ->
                                    val reliefPackage = wrappedReliefPackage.nullableObject
                                    if (originalCardData.tagId == null || originalCardData.tagId.contentEquals(
                                            tag.id
                                        )
                                    ) {
                                        NfcLogger.d(
                                            TAG,
                                            "subtractBalanceFromCard: tag: $tag, value: $amounts, currencyCode: $currency, originalBalance: ${originalCardData.preserveBalance?.totalBalance}, deposit: ${reliefPackage?.convertToDeposit()} "
                                        )
                                        nfcFacade.subtractFromBalance(
                                            tag,
                                            pin,
                                            amounts,
                                            currency,
                                            originalCardData.preserveBalance,
                                            reliefPackage?.convertToDeposit()
                                        ).flatMap { userBalance ->
                                            NfcLogger.d(
                                                TAG,
                                                "subtractedBalanceFromCard: balance: ${userBalance.balance}, beneficiaryId: ${userBalance.userId}, currencyCode: ${userBalance.currencyCode}"
                                            )
                                            if (userBalance.depositDone && reliefPackage != null) {
                                                depositFacade.updateReliefPackageInDB(reliefPackage.apply {
                                                    createdAt = convertTimeForApiRequestBody(Date())
                                                    balanceBefore = userBalance.originalBalance
                                                    balanceAfter = reliefPackage.amount
                                                }).toSingle {
                                                    Pair(
                                                        tag, UserBalance(
                                                            userBalance.userId,
                                                            userBalance.assistanceId,
                                                            userBalance.expirationDate,
                                                            userBalance.currencyCode,
                                                            reliefPackage.amount,
                                                            userBalance.balance,
                                                            userBalance.limits,
                                                            userBalance.depositDone
                                                        )
                                                    )
                                                }
                                            } else {
                                                Single.just(
                                                    Pair(tag, userBalance)
                                                )
                                            }
                                        }
                                    } else {
                                        throw PINException(PINExceptionEnum.DIFFERENT_USER, tag.id)
                                    }
                                }
                        }
                    }
            }
        )
    }

    private fun saveVoucherPurchaseToDb(): Completable {
        return purchaseFacade.savePurchase(createVoucherPurchase())
    }

    private fun createVoucherPurchase(): Purchase {
        return Purchase().apply {
            products.addAll(convert(shoppingHolder.cart))
            vouchers.addAll(shoppingHolder.vouchers.map { it.id })
            vendorId = currentVendor.vendor.vendorId
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
            assistanceId = userBalance.assistanceId.toLong()
            createdAt = convertTimeForApiRequestBody(Date())
            vendorId = currentVendor.vendor.vendorId
            currency = userBalance.currencyCode
            balanceBefore = userBalance.originalBalance
            balanceAfter = userBalance.balance
        }
    }

    private fun convert(cart: MutableList<SelectedProduct>): Collection<PurchasedProduct> {
        return cart.map {
            PurchasedProduct(
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
