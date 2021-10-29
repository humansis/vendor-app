package cz.quanti.android.vendor_app.repository.utils

class PagedApiEntity<E> (
    var totalCount: Long = 0,
    var data: List<E> = listOf()
)
