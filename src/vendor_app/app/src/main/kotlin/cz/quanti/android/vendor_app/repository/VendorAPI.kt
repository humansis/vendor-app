package cz.quanti.android.vendor_app.repository

import cz.quanti.android.vendor_app.repository.booklet.dto.api.BookletApiEntity
import cz.quanti.android.vendor_app.repository.booklet.dto.api.BookletCodesBody
import cz.quanti.android.vendor_app.repository.login.dto.api.SaltApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorApiEntity
import cz.quanti.android.vendor_app.repository.product.dto.api.ProductApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.*
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionApiEntity
import cz.quanti.android.vendor_app.repository.transaction.dto.api.TransactionPurchaseApiEntity
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface VendorAPI {

    @GET("v1/salt/{username}")
    fun getSalt(@Path("username") username: String): Single<Response<SaltApiEntity>>

    @POST("v1/login")
    fun postLogin(@Body vendor: VendorApiEntity): Single<Response<VendorApiEntity>>

    @GET("v1/products")
    fun getProducts(): Single<Response<List<ProductApiEntity>>>

    @GET("v1/deactivated-booklets")
    fun getDeactivatedBooklets(): Single<Response<List<BookletApiEntity>>>

    @GET("v1/protected-booklets")
    fun getProtectedBooklets(): Single<Response<List<BookletApiEntity>>>

    @POST("v1/vouchers/purchase")
    fun postVoucherPurchases(
        @Body voucherPurchases: List<VoucherPurchaseApiEntity>
    ): Single<Response<Unit>>

    @POST("v1/deactivate-booklets")
    fun postBooklets(
        @Body bookletCodes: BookletCodesBody
    ): Single<Response<Unit>>

    @PATCH("v1/smartcards/{id}/purchase")
    fun postCardPurchase(
        @Path("id") cardId: String,
        @Body cardPurchase: CardPurchaseApiEntity
    ): Single<Response<Unit>>

    @GET("v1/smartcards/blocked")
    fun getBlockedCards(): Single<Response<List<String>>>

    @GET("v2/vendors/{id}/smartcard-redemption-batches")
    fun getInvoices(
        @Path("id") vendorId: Int
    ): Single<Response<V2InvoiceApiEntity>>

    @GET("v3/vendors/{id}/smartcard-redemption-candidates")
    fun getTransactions(
        @Path("id") vendorId: Int
    ): Single<Response<List<TransactionApiEntity>>>

    @GET("v1/vendors/{vendorId}/projects/{projectId}/currencies/{curr}/smartcard-purchases")
    fun getTransactionsPurchases(
        @Path("vendorId") vendorId: Int,
        @Path("projectId") projectId: Long,
        @Path("curr") currency: String
    ): Single<Response<List<TransactionPurchaseApiEntity>>>
}
