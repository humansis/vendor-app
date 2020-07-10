package cz.quanti.android.vendor_app.repository

import cz.quanti.android.vendor_app.repository.booklet.dto.api.BookletApiEntity
import cz.quanti.android.vendor_app.repository.booklet.dto.api.BookletCodesBody
import cz.quanti.android.vendor_app.repository.login.dto.api.SaltApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorApiEntity
import cz.quanti.android.vendor_app.repository.product.dto.api.ProductApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.CardPurchaseApiEntity
import cz.quanti.android.vendor_app.repository.purchase.dto.api.VoucherPurchaseApiEntity
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface VendorAPI {

    @GET("salt/{username}")
    fun getSalt(@Path("username") username: String): Single<Response<SaltApiEntity>>

    @POST("login")
    fun postLogin(@Body vendor: VendorApiEntity): Single<Response<VendorApiEntity>>

    @GET("vendors/{id}")
    fun getVendor(@Path("id") id: Long): Single<Response<VendorApiEntity>>

    @GET("products")
    fun getProducts(): Single<Response<List<ProductApiEntity>>>

    @GET("deactivated-booklets")
    fun getDeactivatedBooklets(): Single<Response<List<BookletApiEntity>>>

    @GET("protected-booklets")
    fun getProtectedBooklets(): Single<Response<List<BookletApiEntity>>>

    @POST("vouchers/purchase")
    fun postVoucherPurchases(
        @Body voucherPurchases: List<VoucherPurchaseApiEntity>
    ): Single<Response<Unit>>

    @POST("deactivate-booklets")
    fun postBooklets(
        @Body bookletCodes: BookletCodesBody
    ): Single<Response<Unit>>

    @PATCH("smartcards/{id}/purchase")
    fun postCardPurchase(
        @Path("id") cardId: String,
        @Body cardPurchase: CardPurchaseApiEntity
    ): Single<Response<Unit>>

    @GET("smartcards/blocked")
    fun getBlockedCards(): Single<Response<List<String>>>
}
