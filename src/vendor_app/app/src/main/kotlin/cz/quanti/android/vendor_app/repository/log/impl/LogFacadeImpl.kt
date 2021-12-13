package cz.quanti.android.vendor_app.repository.log.impl

import android.content.Context
import cz.quanti.android.vendor_app.repository.log.LogFacade
import cz.quanti.android.vendor_app.sync.SynchronizationSubject
import cz.quanti.android.vendor_app.utils.VendorAppException
import cz.quanti.android.vendor_app.utils.isPositiveResponseHttpCode
import io.reactivex.Completable
import io.reactivex.Observable
import quanti.com.kotlinlog.file.FileLogger
import quanti.com.kotlinlog.utils.getZipOfLogs

class LogFacadeImpl(
    val logRepo: LogRepositoryImpl,
    val context: Context
) : LogFacade {

    override fun syncWithServer(vendorId: Int): Observable<SynchronizationSubject> {
        return Observable.just(SynchronizationSubject.LOGS_UPLOAD)
            .concatWith(postLogs(vendorId))
    }

    private fun postLogs(vendorId: Int): Completable {
        return logRepo.postLogs(vendorId, getZipOfLogs(context, 48))
            .flatMapCompletable { responseCode ->
                if (isPositiveResponseHttpCode(responseCode)) {
                    Completable.fromCallable { FileLogger.deleteAllLogs(context) }
                } else {
                    throw VendorAppException("Could not upload Logs").apply {
                        this.apiResponseCode = responseCode
                        this.apiError = true
                    }
                }
            }
    }
}
