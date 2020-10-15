package cz.quanti.android.vendor_app.repository.synchronization

import io.reactivex.Completable
import io.reactivex.Single

interface SynchronizationFacade {

    fun synchronize(): Completable

    fun isSyncNeeded(): Single<Boolean>
}
