package cz.quanti.android.vendor_app.sync

import io.reactivex.Observable

interface SynchronizationManager {

    fun synchronizeWithServer()

    fun syncStateObservable(): Observable<SynchronizationState>

    fun resetSyncState()
}
