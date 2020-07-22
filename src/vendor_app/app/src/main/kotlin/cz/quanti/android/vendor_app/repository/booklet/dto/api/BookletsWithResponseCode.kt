package cz.quanti.android.vendor_app.repository.booklet.dto.api

import cz.quanti.android.vendor_app.repository.booklet.dto.Booklet

data class BookletsWithResponseCode (
    val booklets: List<Booklet>,
    val responseCode: Int
)
