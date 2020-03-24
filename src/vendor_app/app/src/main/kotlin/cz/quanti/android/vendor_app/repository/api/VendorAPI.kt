package cz.quanti.android.vendor_app.repository.api

import cz.quanti.android.vendor_app.repository.api.response.BookletsResponse
import cz.quanti.android.vendor_app.repository.api.response.ProductsResponse
import cz.quanti.android.vendor_app.repository.entity.*
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface VendorAPI {

    @GET("salt/{username}")
    fun getSalt(@Path("username") username: String): Single<Response<Salt>>

    @POST("login_app")
    fun postLogin(@Body vendor: Vendor): Single<Response<Vendor>>

    @GET("products")
    fun getProducts(): Single<Response<List<Product>>>

    @GET("deactivate-booklets")
    fun getDeactivatedBooklets(): Single<Response<BookletsResponse>>

    @GET("protected-booklets")
    fun getProtectedBooklets(): Single<Response<BookletsResponse>>

    @POST("vouchers/scanned")
    fun postVouchers(
        @Body vouchers: List<Voucher>
    ): Single<Response<Unit>>

    @POST("deactivate-booklets")
    fun postBooklets(
        @Body booklets: List<Booklet>
    ): Single<Response<Unit>>
}
