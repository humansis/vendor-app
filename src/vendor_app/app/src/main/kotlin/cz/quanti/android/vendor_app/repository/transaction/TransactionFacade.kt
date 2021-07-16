package cz.quanti.android.vendor_app.repository.transaction

import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import io.reactivex.Completable
import io.reactivex.Single

interface TransactionFacade {

    fun syncWithServer(vendorId: Int): Completable

    fun getTransactions(): Single<List<Transaction>>
}
