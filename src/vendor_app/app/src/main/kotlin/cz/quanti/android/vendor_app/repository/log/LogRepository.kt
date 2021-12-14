package cz.quanti.android.vendor_app.repository.log

import io.reactivex.Single
import java.io.File
import retrofit2.Response

interface LogRepository {
    fun postLogs(vendorId: Int, zipOfLogs: File): Single<Response<Unit>>
}
