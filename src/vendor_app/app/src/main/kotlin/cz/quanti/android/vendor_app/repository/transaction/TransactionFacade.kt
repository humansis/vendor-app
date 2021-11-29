package cz.quanti.android.vendor_app.repository.transaction

import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Observable

interface TransactionFacade {

    fun syncWithServer(
        vendorId: Int
    ): Observable<SynchronizationSubject>

    fun getTransactions(): Observable<List<Transaction>>

    fun deleteTransactions(): Completable
}
