package cz.quanti.android.vendor_app.sync

import androidx.lifecycle.LiveData
import io.reactivex.Observable

interface SynchronizationManager {

    fun synchronizeWithServer()

    fun syncStateObservable(): Observable<SynchronizationState>

    fun syncSubjectObservable(): Observable<SynchronizationSubject>

    fun resetSyncState()

    fun showDot(): LiveData<Boolean>
    fun getLastSyncError(): Throwable?
}
