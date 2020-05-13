package cz.quanti.android.vendor_app.repository

import cz.quanti.android.vendor_app.repository.login.dto.api.SaltApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorApiEntity
import cz.quanti.android.vendor_app.repository.product.dto.api.ProductApiEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.api.BookletApiEntity
import cz.quanti.android.vendor_app.repository.voucher.dto.api.BookletCodesBody
import cz.quanti.android.vendor_app.repository.voucher.dto.api.VoucherApiEntity
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VendorAPI {

    @GET("salt/{username}")
    fun getSalt(@Path("username") username: String): Single<Response<SaltApiEntity>>

    @POST("login")
    fun postLogin(@Body vendor: VendorApiEntity): Single<Response<VendorApiEntity>>

    @GET("vendors/{id}")
    fun getVendor(@Path("id") id: String): Single<Response<VendorApiEntity>>

    @GET("products")
    fun getProducts(): Single<Response<List<ProductApiEntity>>>

    @GET("deactivated-booklets")
    fun getDeactivatedBooklets(): Single<Response<List<BookletApiEntity>>>

    @GET("protected-booklets")
    fun getProtectedBooklets(): Single<Response<List<BookletApiEntity>>>

    @POST("vouchers/scanned")
    fun postVouchers(
        @Body vouchers: List<VoucherApiEntity>
    ): Single<Response<Unit>>

    @POST("deactivate-booklets")
    fun postBooklets(
        @Body bookletCodes: BookletCodesBody
    ): Single<Response<Unit>>
}
