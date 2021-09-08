package cz.quanti.android.vendor_app.sync

import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import quanti.com.kotlinlog.Log
import java.util.Date

class SynchronizationManagerImpl(
    private val preferences: AppPreferences,
    private val syncFacade: SynchronizationFacade
) : SynchronizationManager {

    private val syncStatePublishSubject = BehaviorSubject.createDefault(SynchronizationState.INIT)

    override fun synchronizeWithServer() {
        if (syncStatePublishSubject.value == SynchronizationState.STARTED) {
            Log.e(TAG, "Synchronization already in progress")
        } else {
            syncStatePublishSubject.onNext(SynchronizationState.STARTED)
            syncFacade.synchronize(preferences.vendor.id.toInt())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                    {
                        preferences.lastSynced = Date().time
                        syncStatePublishSubject.onNext(SynchronizationState.SUCCESS)
                        Log.e(TAG, "Synchronization finished successfully")
                    },
                    { e ->
                        Log.e(TAG, e)
                        syncStatePublishSubject.onNext(SynchronizationState.ERROR)
                    }
                ).let { /*ignore disposable*/ }
        }
    }

    override fun syncStateObservable(): Observable<SynchronizationState> {
        return syncStatePublishSubject
    }

    override fun resetSyncState() {
        syncStatePublishSubject.onNext(SynchronizationState.INIT)
    }

    companion object {
        private val TAG = SynchronizationManagerImpl::class.java.simpleName
    }
}
