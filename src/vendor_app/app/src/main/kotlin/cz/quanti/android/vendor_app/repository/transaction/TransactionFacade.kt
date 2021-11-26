package cz.quanti.android.vendor_app.repository.transaction

import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface TransactionFacade {

    fun syncWithServer(
        vendorId: Int
    ): Completable

    fun getTransactions(): Observable<List<Transaction>>

    fun deleteTransactions(): Completable

    fun getSyncSubject(): PublishSubject<SynchronizationSubject>
}
