package cz.quanti.android.vendor_app.repository.log.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.log.LogRepository
import io.reactivex.Single
import java.io.File
import java.io.FileInputStream
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Response

class LogRepositoryImpl(
    val api: VendorAPI
) : LogRepository {
    override fun postLogs(vendorId: Int, zipOfLogs: File): Single<Response<Unit>> {
        return api.postLogs(
            vendorId,
            RequestBody.create(MediaType.parse("multipart/form-data"), zipOfLogs)
        )
    }
}
