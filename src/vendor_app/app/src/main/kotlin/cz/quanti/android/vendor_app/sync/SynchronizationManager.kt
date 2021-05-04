package cz.quanti.android.vendor_app.sync

import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface SynchronizationManager {

    fun synchronizeWithServer()

    fun syncStateSubject(): Subject<SynchronizationState>

    fun syncStateObservable(): Observable<SynchronizationState>
}
