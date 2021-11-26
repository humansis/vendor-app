package cz.quanti.android.vendor_app.repository.transaction

import cz.quanti.android.vendor_app.repository.transaction.dto.Transaction
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

interface TransactionFacade {

    fun syncWithServer(
        syncSubjectReplaySubject: ReplaySubject<SynchronizationSubject>,
        vendorId: Int
    ): Completable

    fun getTransactions(): Observable<List<Transaction>>

    fun deleteTransactions(): Completable
}
