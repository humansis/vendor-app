package cz.quanti.android.vendor_app.main.checkout.viewmodel

import android.nfc.Tag
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
    private val currentVendor: CurrentVendor
) : ViewModel() {
    private var vouchers: MutableList<Voucher> = mutableListOf()
    private var pin: String? = null
    private val originalBalanceLD = MutableLiveData<Double?>(null)
    private val originalTagLD = MutableLiveData<Tag?>(null)
    private val isScanningInProgressLD = MutableLiveData(false)

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

    fun setScanningInProgress(inProgress: Boolean) {
        isScanningInProgressLD.value = inProgress
    }

    fun getScanningInProgress(): MutableLiveData<Boolean> {
        return isScanningInProgressLD
    }

    fun setOriginalBalance(originalBalance: Double?) {
        this.originalBalanceLD.value = originalBalance // TODO zapsat do db + timestamp.. pokud null tak posledni zaznam smazat (bude asi potreba pridat userid pro identifikaci)
    // prevent this user from creating new purchase until he is removed from db (allow finishing interrupted payment but prevent new purchase)
    // -> allow if originalbalance == userid v db ale ne, pokud originalbalance == null
        // ma vubec smysl to ukladat do db s nejakym casovym limitem? asi ne
    }

    fun getOriginalBalance(): MutableLiveData<Double?> {
        return originalBalanceLD
    }

    fun setOriginalTag(originalTag: Tag?) {
        this.originalTagLD.value = originalTag
    }

    fun getVouchers(): List<Voucher> {
        return vouchers
    }

    fun clearVouchers() {
        vouchers.clear()
    }

    fun getSelectedProducts(): LiveData<List<SelectedProduct>> {
        return shoppingHolder.getProducts()
    }

    fun setProducts(products: List<SelectedProduct>) {
        shoppingHolder.cart.clear()
        shoppingHolder.cart.addAll(products)
    }

    fun updateSelectedProduct(product: SelectedProduct) {
        shoppingHolder.updateProduct(product)
    }

    fun removeFromCart(product: SelectedProduct) {
        shoppingHolder.removeProductAt(product)
    }

    fun clearCart() {
        shoppingHolder.removeAllProducts()
    }

    fun getCurrency(): LiveData<String> {
        return shoppingHolder.chosenCurrency
    }

    fun getPin(): String? {
        return pin
    }
    fun setPin(string: String?) {
        this.pin = string
    }

    fun payByCard(tag: Tag, pin: String, value: Double, currency: String): Single<UserBalance> {
        return subtractMoneyFromCard(tag, pin, value, currency).flatMap { userBalance ->
            saveCardPurchaseToDb(convertTagToString(tag))
                .subscribeOn(Schedulers.io())
                .toSingleDefault(userBalance)
                .flatMap {
                    Single.just(it)
                }
        }
    }

    private fun convertTagToString(tag: Tag): String {
        return NfcUtil.toHexString(tag.id).uppercase(Locale.US)
    }

    private fun subtractMoneyFromCard(
        tag: Tag,
        pin: String,
        value: Double,
        currency: String
    ): Single<UserBalance> {
        if (originalTagLD.value == null) setOriginalTag(tag) // TODO remove after exception returns tagid
        return cardFacade.getBlockedCards()
            .subscribeOn(Schedulers.io())
            .flatMap {
            if(it.contains(convertTagToString(tag))) {
                throw PINException(PINExceptionEnum.CARD_LOCKED)
            } else {
                NfcLogger.d(
                    TAG,
                    "subtractBalanceFromCard: value: $value, currencyCode: $currency, originalBalance: $originalBalanceLD"
                )
                if (originalTagLD.value?.id == null || originalTagLD.value?.id.contentEquals( tag.id )) {
                    nfcFacade.subtractFromBalance(tag, pin, value, currency, originalBalanceLD.value).map { userBalance ->
                        NfcLogger.d(
                            TAG,
                            "subtractedBalanceFromCard: balance: ${userBalance.balance}, beneficiaryId: ${userBalance.userId}, currencyCode: ${userBalance.currencyCode}"
                        )
                        userBalance
                    }
                } else {
                    throw PINException(PINExceptionEnum.INVALID_DATA)
                }
            }
        }
    }

    private fun saveVoucherPurchaseToDb(): Completable {
        return purchaseFacade.savePurchase(createVoucherPurchase())
    }

    private fun createVoucherPurchase(): Purchase {
        return Purchase().apply {
            products.addAll(shoppingHolder.cart)
            vouchers.addAll(shoppingHolder.vouchers.map { it.id })
            vendorId = currentVendor.vendor.id
            createdAt = convertTimeForApiRequestBody(Date())
            currency = getCurrency().toString()
        }
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
            currency = getCurrency().toString()
        }
    }

    companion object {
        private val TAG = CheckoutViewModel::class.java.simpleName
    }
}
