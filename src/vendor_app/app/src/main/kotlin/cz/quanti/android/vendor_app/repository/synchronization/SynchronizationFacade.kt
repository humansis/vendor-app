package cz.quanti.android.vendor_app.repository.synchronization

import io.reactivex.Completable

interface SynchronizationFacade {

    fun synchronize(): Completable
}
