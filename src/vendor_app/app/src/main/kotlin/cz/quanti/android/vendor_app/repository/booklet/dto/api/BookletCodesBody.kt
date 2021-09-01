package cz.quanti.android.vendor_app.repository.booklet.dto.api

import com.google.gson.annotations.SerializedName

data class BookletCodesBody(
    @SerializedName("bookletCodes")
    var bookletCodes: List<String>
)
