package cz.quanti.android.vendor_app.sync

import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import quanti.com.kotlinlog.Log
import java.util.*

class SynchronizationManagerImpl(
    private val preferences: AppPreferences,
    private val syncFacade: SynchronizationFacade
) : SynchronizationManager {

    private val syncStatePublishSubject = PublishSubject.create<SynchronizationState>()

    override fun synchronizeWithServer() {
        syncStatePublishSubject.onNext(SynchronizationState.STARTED)
        syncFacade.synchronize(preferences.vendor.id.toInt())
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(
                {
                    preferences.lastSynced = Date().time
                    syncStatePublishSubject.onNext(SynchronizationState.SUCCESS)
                },
                { e ->
                    Log.e(e)
                    syncStatePublishSubject.onNext(SynchronizationState.ERROR)
                }
            ).let { /*ignore disposable*/ }
    }

    override fun syncStateSubject(): Subject<SynchronizationState> {
        return syncStatePublishSubject
    }

    override fun syncStateObservable(): Observable<SynchronizationState> {
        return syncStatePublishSubject
    }
}
