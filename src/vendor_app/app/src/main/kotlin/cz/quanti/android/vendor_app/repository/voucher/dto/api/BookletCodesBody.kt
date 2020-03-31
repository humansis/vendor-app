package cz.quanti.android.vendor_app.repository.voucher.dto.api

import com.google.gson.annotations.SerializedName

data class BookletCodesBody(
    @SerializedName("bookletCodes")
    var bookletCodes: List<String>
)


