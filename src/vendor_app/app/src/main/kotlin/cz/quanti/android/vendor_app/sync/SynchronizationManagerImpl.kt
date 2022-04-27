package cz.quanti.android.vendor_app.sync

import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import cz.quanti.android.vendor_app.repository.AppPreferences
import cz.quanti.android.vendor_app.repository.synchronization.SynchronizationFacade
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.lang.Exception
import java.util.Date
import quanti.com.kotlinlog.Log

class SynchronizationManagerImpl(
    private val preferences: AppPreferences,
    private val syncFacade: SynchronizationFacade
) : SynchronizationManager {

    private val syncStatePublishSubject = BehaviorSubject.createDefault(SynchronizationState.INIT)
    private val syncSubject = BehaviorSubject.create<SynchronizationSubject>()
    private var lastSyncError: Throwable? = null

    override fun synchronizeWithServer() {
        if (syncStatePublishSubject.value == SynchronizationState.STARTED) {
            Log.d(TAG, "Synchronization already in progress")
        } else {
            Log.d(TAG, "Synchronization started")
            lastSyncError = null
            syncStatePublishSubject.onNext(SynchronizationState.STARTED)
            val vendorId = preferences.vendor.vendorId.toInt()
            syncFacade.synchronize(vendorId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                    { subject ->
                        syncSubject.onNext(subject)
                    },
                    { e ->
                        if (e is ExceptionWithReason) {
                            Log.e(TAG, e, e.reason)
                        } else {
                            Log.e(TAG, e)
                        }
                        lastSyncError = e
                        syncStatePublishSubject.onNext(SynchronizationState.ERROR)
                    },
                    {
                        preferences.lastSynced = Date().time
                        syncStatePublishSubject.onNext(SynchronizationState.SUCCESS)
                        Log.d(TAG, "Synchronization finished successfully")
                    }
                ).let { /*ignore disposable*/ }
        }
    }

    override fun syncStateObservable(): Observable<SynchronizationState> {
        return syncStatePublishSubject
    }

    override fun syncSubjectObservable(): Observable<SynchronizationSubject> {
        return syncSubject
    }

    override fun getLastSyncError(): Throwable? {
        return lastSyncError
    }

    override fun resetSyncState() {
        syncStatePublishSubject.onNext(SynchronizationState.INIT)
    }

    override fun showDot(): LiveData<Boolean> {
        return syncFacade.isSyncNeeded().toFlowable(BackpressureStrategy.LATEST).toLiveData()
    }

    override fun getPurchasesCount(): Observable<Long> {
        return syncFacade.getPurchasesCount()
    }

    class ExceptionWithReason(
        throwable: Throwable,
        val reason: String
    ) : Exception(throwable)

    companion object {
        private val TAG = SynchronizationManagerImpl::class.java.simpleName
    }
}
